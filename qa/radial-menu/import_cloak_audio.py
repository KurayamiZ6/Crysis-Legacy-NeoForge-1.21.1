"""Import a specifically identified MPEG cue using the installed Audacity codecs.
Remastered archives are read only; extraction verifies ZIP size/CRC and FSB4 headers.
"""
import argparse
import ctypes as C
import hashlib
import json
import os
from pathlib import Path
import struct
import tempfile
import zipfile
import zlib
from export_classic_transitions import Info

def read_pak(archive, name):
    info=archive.getinfo(name)
    archive.fp.seek(info.header_offset)
    header=struct.unpack('<IHHHHHIIIHH',archive.fp.read(30))
    assert header[0]==0x04034b50 and not (info.flag_bits&1)
    # CryPak stores backslashes in local headers and slashes in its directory.
    archive.fp.seek(info.header_offset+30+header[-2]+header[-1])
    raw=archive.fp.read(info.compress_size)
    data=zlib.decompress(raw,-15) if info.compress_type==8 else raw
    assert len(data)==info.file_size and zlib.crc32(data)&0xffffffff==info.CRC
    return data

def extract_activation(game):
    with zipfile.ZipFile(game/'Game/sounds.pak') as archive:
        bank=read_pak(archive,'sounds/interface/interface_combined.fsb')
    assert bank[:4]==b'FSB4'
    count,headers=struct.unpack_from('<2I',bank,4)
    header,offset=48,48+headers
    for _ in range(count):
        size=struct.unpack_from('<H',bank,header)[0]
        name=bank[header+2:header+32].split(b'\0')[0].decode('ascii')
        frames,length=struct.unpack_from('<2I',bank,header+32)
        if name=='cloak_activate':
            return bank[offset:offset+length],frames,'Crysis Remastered/Game/sounds.pak::sounds/interface/interface_combined.fsb::cloak_activate'
        header+=size;offset=(offset+length+31)//32*32
    raise ValueError('cloak_activate not found')

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    sources=parser.add_mutually_exclusive_group(required=True)
    sources.add_argument('--game-root',type=Path)
    sources.add_argument('--mpeg',type=Path)
    parser.add_argument('--audacity',type=Path,default=Path('C:/Program Files/Audacity'))
    parser.add_argument('--output',type=Path,required=True)
    parser.add_argument('--manifest',type=Path,required=True)
    parser.add_argument('--origin',help='Canonical archive/member identifier for a separately extracted voice')
    args=parser.parse_args()
    if args.game_root: payload,expected_frames,origin=extract_activation(args.game_root)
    else: payload=args.mpeg.read_bytes();expected_frames=None;origin=str(args.mpeg)
    if args.origin:origin=args.origin
    root=Path(__file__).resolve().parents[2]
    with tempfile.NamedTemporaryFile(suffix='.mp2',dir=root/'build/radial-menu',delete=False) as temp:
        temp.write(payload);temporary=Path(temp.name)
    try:
        with os.add_dll_directory(str(args.audacity.resolve())):
            m=C.CDLL(str(args.audacity.resolve()/'mpg123.dll'));m.mpg123_init()
            m.mpg123_new.argtypes=[C.c_char_p,C.POINTER(C.c_int)];m.mpg123_new.restype=C.c_void_p
            m.mpg123_open.argtypes=[C.c_void_p,C.c_char_p]
            m.mpg123_getformat.argtypes=[C.c_void_p,C.POINTER(C.c_long),C.POINTER(C.c_int),C.POINTER(C.c_int)]
            m.mpg123_read.argtypes=[C.c_void_p,C.c_void_p,C.c_size_t,C.POINTER(C.c_size_t)]
            m.mpg123_delete.argtypes=[C.c_void_p]
            error=C.c_int();handle=m.mpg123_new(None,C.byref(error));assert handle
            try:
                assert m.mpg123_open(handle,str(temporary).encode())==0
                rate=C.c_long();channels=C.c_int();encoding=C.c_int()
                assert m.mpg123_getformat(handle,C.byref(rate),C.byref(channels),C.byref(encoding))==0
                assert encoding.value==208
                buffer=C.create_string_buffer(32768);done=C.c_size_t();pcm=bytearray()
                while True:
                    result=m.mpg123_read(handle,buffer,len(buffer),C.byref(done));pcm.extend(buffer.raw[:done.value])
                    if result==-12: break
                    assert result in (0,-11)
            finally:m.mpg123_delete(handle)
            frames=len(pcm)//(channels.value*2)
            if expected_frames is not None:assert frames==expected_frames
            lib=C.CDLL(str(args.audacity.resolve()/'sndfile.dll'))
            lib.sf_open.argtypes=[C.c_char_p,C.c_int,C.POINTER(Info)];lib.sf_open.restype=C.c_void_p
            lib.sf_writef_short.argtypes=[C.c_void_p,C.POINTER(C.c_short),C.c_longlong];lib.sf_writef_short.restype=C.c_longlong
            lib.sf_readf_short.argtypes=[C.c_void_p,C.POINTER(C.c_short),C.c_longlong];lib.sf_readf_short.restype=C.c_longlong
            lib.sf_close.argtypes=[C.c_void_p]
            args.output.parent.mkdir(parents=True,exist_ok=True)
            info=Info(0,rate.value,channels.value,0x010002 if args.output.suffix=='.wav' else 0x200060,0,0)
            output=lib.sf_open(str(args.output.resolve()).encode(),0x20,C.byref(info));assert output
            samples=(C.c_short*(len(pcm)//2)).from_buffer_copy(pcm)
            assert lib.sf_writef_short(output,samples,frames)==frames
            assert lib.sf_close(output)==0
            check=Info();output=lib.sf_open(str(args.output.resolve()).encode(),0x10,C.byref(check));assert output
            assert (check.frames,check.samplerate,check.channels)==(frames,rate.value,channels.value)
            lib.sf_close(output)
        metadata=dict(source=origin,frames=frames,rate=rate.value,channels=channels.value,duration=frames/rate.value,
            source_payload_sha256=hashlib.sha256(payload).hexdigest(),output_sha256=hashlib.sha256(args.output.read_bytes()).hexdigest(),
            decoded_and_roundtrip_metadata='PASS')
        args.manifest.write_text(json.dumps(metadata,indent=2)+'\n',encoding='utf-8')
        print(json.dumps(metadata,indent=2))
    finally:temporary.unlink()  # Only the temporary file created above in the workspace.

if __name__=='__main__':main()
