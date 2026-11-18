"""Check the FSB IMA decoder against libsndfile's independent WAV IMA decoder.
Also check the three delivered Ogg streams' frame counts, rates and channels.
Run from repository root with the installed Audacity libraries (no playback claim).
"""
from pathlib import Path
import ctypes as C
import json
import os
import struct
import tempfile
import numpy as np
from export_classic_transitions import Info, decode_ima

root = Path(__file__).resolve().parents[2]
bank = (root.parent/'Original games sounds/Crysis Sound, VO & Music/Sounds/interface/interface_suit.fsb').read_bytes()
manifest = json.loads((root/'qa/radial-menu/classic-transition-sources.json').read_text())
count, headers = struct.unpack_from('<2I', bank, 4)
header, offset = 24, 24+headers
report = {}
with os.add_dll_directory('C:/Program Files/Audacity'):
    lib = C.CDLL('C:/Program Files/Audacity/sndfile.dll')
    lib.sf_open.argtypes = [C.c_char_p,C.c_int,C.POINTER(Info)]; lib.sf_open.restype = C.c_void_p
    lib.sf_readf_short.argtypes = [C.c_void_p,C.POINTER(C.c_short),C.c_longlong]
    lib.sf_readf_short.restype = C.c_longlong
    lib.sf_close.argtypes = [C.c_void_p]
    def read(path):
        info=Info(); handle=lib.sf_open(str(path).encode(),0x10,C.byref(info)); assert handle
        samples=(C.c_short*(info.frames*info.channels))()
        assert lib.sf_readf_short(handle,samples,info.frames)==info.frames
        lib.sf_close(handle)
        return info,np.ctypeslib.as_array(samples).reshape(-1,info.channels).copy()
    for _ in range(count):
        size=struct.unpack_from('<H',bank,header)[0]
        name=bank[header+2:header+32].split(b'\0')[0].decode('ascii')
        frames,length,_,_,mode,rate=struct.unpack_from('<6I',bank,header+32)
        channels=struct.unpack_from('<H',bank,header+62)[0]
        cue=next((cue for cue,data in manifest.items() if data.get('sample')==name),None)
        if cue:
            payload=bank[offset:offset+length]
            pcm=np.array(decode_ima(payload,channels,frames),dtype=np.int16).reshape(-1,channels)
            block_size=36*channels
            blocks=(frames+63)//64
            fmt=struct.pack('<HHIIHHHH',17,channels,rate,rate*block_size//65,block_size,4,2,65)
            tail=b'WAVEfmt '+struct.pack('<I',len(fmt))+fmt+b'fact'+struct.pack('<II',4,blocks*65)
            tail+=b'data'+struct.pack('<I',blocks*block_size)+payload[:blocks*block_size]
            with tempfile.NamedTemporaryFile(suffix='.wav',delete=False,dir=root/'build/radial-menu') as temporary:
                temporary.write(b'RIFF'+struct.pack('<I',len(tail))+tail); path=Path(temporary.name)
            try: _,native=read(path)
            finally: path.unlink()  # Only this explicitly created temporary WAV.
            # WAV includes the initial predictor sample; FSB encodes 64 following samples.
            native=native.reshape(blocks,65,channels)[:,1:,:].reshape(-1,channels)[:frames]
            correlation=float(np.corrcoef(native.ravel(),pcm.ravel())[0,1])
            assert correlation>.9999,(cue,correlation)
            info,ogg=read(root/'src/main/resources/assets/crysismod/sounds/classic'/f'{cue}.ogg')
            assert (info.frames,info.samplerate,info.channels)==(frames,rate,channels)
            report[cue]={'independent_decoder_correlation':correlation,'ogg_frames':info.frames,
                         'rate':info.samplerate,'channels':info.channels,'status':'PASS'}
        header+=size;offset+=length
    for cue,data in manifest.items():
        if cue in report:continue
        path=root/'src/main/resources/assets/crysismod/sounds/classic'/f'{cue}.ogg'
        info,ogg=read(path)
        assert (info.frames,info.samplerate,info.channels)==(data['frames'],data['rate'],data['channels'])
        import hashlib
        assert hashlib.sha256(path.read_bytes()).hexdigest()==data['output_sha256']
        report[cue]={'source':data['source'],'ogg_frames':info.frames,'rate':info.samplerate,
                     'channels':info.channels,'status':'PASS','decoder':'MPEG; source CRC/frame count verified by importer'}
assert len(report)==3
(root/'qa/radial-menu/classic-transition-audio-checks.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
