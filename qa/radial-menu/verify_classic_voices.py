"""Identify the existing Ogg voices against the supplied English Crysis 1 MP2s.
Run from the repository root with the installed Audacity DLLs and NumPy.
MPEG decoding can prepend one frame: compare alignments in 1152-sample blocks.
This compares waveforms and does not assert an auditory validation of wording.
"""
from pathlib import Path
import ctypes as C,os,json
import numpy as np
class Info(C.Structure):_fields_=[('frames',C.c_longlong),('samplerate',C.c_int),('channels',C.c_int),('format',C.c_int),('sections',C.c_int),('seekable',C.c_int)]
with os.add_dll_directory('C:/Program Files/Audacity'):
 m=C.CDLL('C:/Program Files/Audacity/mpg123.dll');m.mpg123_init();m.mpg123_new.argtypes=[C.c_char_p,C.POINTER(C.c_int)];m.mpg123_new.restype=C.c_void_p;m.mpg123_open.argtypes=[C.c_void_p,C.c_char_p];m.mpg123_getformat.argtypes=[C.c_void_p,C.POINTER(C.c_long),C.POINTER(C.c_int),C.POINTER(C.c_int)];m.mpg123_read.argtypes=[C.c_void_p,C.c_void_p,C.c_size_t,C.POINTER(C.c_size_t)];m.mpg123_delete.argtypes=[C.c_void_p]
 s=C.CDLL('C:/Program Files/Audacity/sndfile.dll');s.sf_open.argtypes=[C.c_char_p,C.c_int,C.POINTER(Info)];s.sf_open.restype=C.c_void_p;s.sf_readf_short.argtypes=[C.c_void_p,C.POINTER(C.c_short),C.c_longlong];s.sf_close.argtypes=[C.c_void_p]
 report={}
 for original,target in [('suit_full_cloak_on','cloak_on'),('suit_full_cloak_off','cloak_off'),('suit_maximum_armor','maximum_armor'),('suit_maximum_strength','maximum_strength'),('suit_maximum_speed','maximum_speed')]:
  path=Path('../Original games sounds/Crysis Sound, VO & Music/Languages/English/dialog/suit/male')/(original+'.mp2');err=C.c_int();h=m.mpg123_new(None,C.byref(err));assert m.mpg123_open(h,str(path.resolve()).encode())==0
  rate=C.c_long();ch=C.c_int();enc=C.c_int();assert m.mpg123_getformat(h,C.byref(rate),C.byref(ch),C.byref(enc))==0;assert enc.value==208,enc.value
  buf=C.create_string_buffer(32768);got=C.c_size_t();raw=bytearray()
  while True:
   result=m.mpg123_read(h,buf,len(buf),C.byref(got));raw.extend(buf.raw[:got.value])
   if result==-12:break
   assert result in (0,-11),result
  m.mpg123_delete(h);pcm=np.frombuffer(raw,dtype='<i2').reshape(-1,ch.value).astype(float)
  info=Info();f=s.sf_open(str(Path('src/main/resources/assets/crysismod/sounds/classic')/(target+'.ogg')).encode(),0x10,C.byref(info));assert f;arr=(C.c_short*(info.frames*info.channels))();s.sf_readf_short(f,arr,info.frames);s.sf_close(f);ogg=np.ctypeslib.as_array(arr).reshape(-1,info.channels).astype(float)
  candidates=[]
  for lag in [-2304,-1152,0,1152,2304]:
   a=pcm[max(0,lag):];b=ogg[max(0,-lag):];n=min(len(a),len(b));candidates.append((float(np.corrcoef(a[:n].ravel(),b[:n].ravel())[0,1]),lag))
  corr,lag=max(candidates);report[target]={'source':str(path).replace('\\','/'),'rate':rate.value,'channels':ch.value,'source_frames':len(pcm),'ogg_frames':len(ogg),'decoder_alignment_frames':lag,'pcm_correlation':corr,'audio_identity_match':corr>.98}
 Path('qa/radial-menu/classic-voice-verification.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf8');print(json.dumps(report,indent=2))

