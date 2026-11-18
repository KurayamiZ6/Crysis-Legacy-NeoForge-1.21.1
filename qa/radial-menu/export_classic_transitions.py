"""Extract only three Crysis 1 transition cues; use the installed Audacity DLLs.

Usage: python export_classic_transitions.py --bank <interface_suit.fsb> --audacity <directory>
FSB3 IMA ADPCM: 64 decoded samples/channel per 36-byte channel block.
No installation, source bank modification, normalization, or dependency download.
"""
import argparse
import ctypes as C
import hashlib
import json
import os
from pathlib import Path
import struct

INDEX = (-1, -1, -1, -1, 2, 4, 6, 8)
STEPS = (7,8,9,10,11,12,13,14,16,17,19,21,23,25,28,31,34,37,41,45,50,55,60,66,73,80,88,97,107,118,130,143,157,173,190,209,230,253,279,307,337,371,408,449,494,544,598,658,724,796,876,963,1060,1166,1282,1411,1552,1707,1878,2066,2272,2499,2749,3024,3327,3660,4026,4428,4871,5358,5894,6484,7132,7845,8630,9493,10442,11487,12635,13899,15289,16818,18500,20350,22385,24623,27086,29794,32767)

class Info(C.Structure):
    _fields_ = [('frames', C.c_longlong), ('samplerate', C.c_int), ('channels', C.c_int),
                ('format', C.c_int), ('sections', C.c_int), ('seekable', C.c_int)]

def decode_ima(payload, channels, frames):
    assert channels in (1, 2)
    encoded_bytes = ((frames + 63)//64) * 36 * channels
    assert 0 <= len(payload) - encoded_bytes < 16  # FSB samples are padded to 16-byte boundaries.
    payload = payload[:encoded_bytes]
    output = []
    for offset in range(0, len(payload), 36 * channels):
        block = payload[offset:offset+36*channels]
        predictors, indices, decoded = [], [], [[] for _ in range(channels)]
        for channel in range(channels):
            value, index, reserved = struct.unpack_from('<hBB', block, channel*4)
            assert index <= 88 and reserved == 0
            predictors.append(value); indices.append(index)
        # Four encoded bytes/channel alternate after the channel headers.
        for group in range(8):
            for channel in range(channels):
                start = channels*4 + (group*channels+channel)*4
                for byte in block[start:start+4]:
                    for nibble in (byte & 15, byte >> 4):
                        step = STEPS[indices[channel]]
                        delta = step >> 3
                        if nibble & 1: delta += step >> 2
                        if nibble & 2: delta += step >> 1
                        if nibble & 4: delta += step
                        value = predictors[channel] + (-delta if nibble & 8 else delta)
                        predictors[channel] = max(-32768, min(32767, value))
                        indices[channel] = max(0, min(88, indices[channel] + INDEX[nibble & 7]))
                        decoded[channel].append(predictors[channel])
        for frame in zip(*decoded): output.extend(frame)
    return output[:frames*channels]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--bank', type=Path, required=True)
    parser.add_argument('--audacity', type=Path, required=True)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    bank = args.bank.read_bytes()
    assert bank[:4] == b'FSB3'
    count, header_bytes = struct.unpack_from('<2I', bank, 4)
    header, data = 24, 24+header_bytes
    wanted = {'suit_speed_activate_07_good.wa': 'speed_activation',
              'cloak_chameleon_activate_12.wa': 'cloak_activation',
              'suit_cloak_chameleon_101_r.wav': 'cloak_deactivation'}
    output = root/'src/main/resources/assets/crysismod/sounds/classic'
    manifest = {}
    with os.add_dll_directory(str(args.audacity.resolve())):
        lib = C.CDLL(str(args.audacity.resolve()/'sndfile.dll'))
        lib.sf_open.argtypes = [C.c_char_p, C.c_int, C.POINTER(Info)]
        lib.sf_open.restype = C.c_void_p
        lib.sf_writef_short.argtypes = [C.c_void_p, C.POINTER(C.c_short), C.c_longlong]
        lib.sf_writef_short.restype = C.c_longlong
        lib.sf_close.argtypes = [C.c_void_p]
        for _ in range(count):
            size = struct.unpack_from('<H', bank, header)[0]
            name = bank[header+2:header+32].split(b'\0')[0].decode('ascii')
            frames, length, _, _, mode, rate = struct.unpack_from('<6I', bank, header+32)
            channels = struct.unpack_from('<H', bank, header+62)[0]
            if name in wanted:
                assert mode & 0x00400000, 'Expected FMOD IMA ADPCM'
                payload = bank[data:data+length]
                assert frames == length//(36*channels)*64
                pcm = decode_ima(payload, channels, frames)
                info = Info(0, rate, channels, 0x200060, 0, 0)  # OGG / VORBIS
                target = output/(wanted[name]+'.ogg')
                handle = lib.sf_open(str(target.resolve()).encode(), 0x20, C.byref(info))
                assert handle, 'Cannot open Vorbis encoder'
                samples = (C.c_short*len(pcm))(*pcm)
                assert lib.sf_writef_short(handle, samples, frames) == frames
                assert lib.sf_close(handle) == 0
                manifest[wanted[name]] = dict(bank='Crysis Sound, VO & Music/Sounds/interface/interface_suit.fsb',
                    sample=name, frames=frames, rate=rate, channels=channels, duration=frames/rate,
                    source_payload_sha256=hashlib.sha256(payload).hexdigest(),
                    ogg_sha256=hashlib.sha256(target.read_bytes()).hexdigest(),
                    auditory_validation='PENDING; deactivation association inferred from the two available cloak cues')
            header += size; data += length
    assert len(manifest) == 3
    (root/'qa/radial-menu/classic-transition-sources.json').write_text(json.dumps(manifest, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(manifest, indent=2))

if __name__ == '__main__': main()
