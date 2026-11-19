"""Verify the packaged, independent armor announcements against the original mod.

This is an asset/routing check, not an assertion about captured audible output.
"""
import hashlib
import json
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parents[2]
entry = "assets/crysismod/sounds/max_armor.ogg"
with zipfile.ZipFile(root.parent / "Original Mod/crysis_legacy-1.1.1-1.20.1.jar") as original:
    expected = original.read(entry)
with zipfile.ZipFile(root / "build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar") as jar:
    n2 = jar.read(entry)
    n1 = jar.read("assets/crysismod/sounds/classic/maximum_armor.ogg")
    sounds = json.loads(jar.read("assets/crysismod/sounds.json"))
assert n2 == expected, "N2 must retain the original mod's armor voice"
assert n1 != n2, "The two suit generations must not share this recording"
assert sounds["maximum_armor"]["sounds"][0]["name"] == "crysismod:max_armor"
assert sounds["classic_maximum_armor"]["sounds"] == ["crysismod:classic/maximum_armor"]
result = {
    "result": "PASS",
    "n2_original_sha256": hashlib.sha256(n2).hexdigest(),
    "n1_preserved_sha256": hashlib.sha256(n1).hexdigest(),
    "scope": "Packaged assets and independent sound mappings; no loopback recording",
}
print(json.dumps(result, indent=2))
