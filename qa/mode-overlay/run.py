"""Capture suit overlays in a copy of the existing graphical test world."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import hashlib

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--run-name", required=True)
parser.add_argument("--mod-jar", type=Path, required=True)
parser.add_argument("--java-home", type=Path, required=True)
parser.add_argument("--compiler-home", type=Path, help="JDK used to build the probe when the runtime is a JRE")
parser.add_argument("--sodium", type=Path, required=True)
parser.add_argument("--template-game", type=Path, default=Path("build/amd-visual-diagnostic/game"))
parser.add_argument("--extra-mod", type=Path, action="append", default=[])
parser.add_argument("--shader-pack", type=Path)
parser.add_argument("--probe", type=Path, default=Path(__file__).with_name("ModeOverlayQa.java"))
parser.add_argument("--probe-mod-id", default="crysismodeoverlayqa")
parser.add_argument("--width", type=int)
parser.add_argument("--height", type=int)
parser.add_argument("--jvm-arg", action="append", default=[])
args = parser.parse_args()
if not args.run_name or any(c not in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-" for c in args.run_name):
    parser.error("--run-name must contain only letters, digits and hyphens")
project = Path(__file__).resolve().parents[2]
run = project / "build/mode-overlay-qa" / args.run_name
game = run / "game"
classes = run / "classes"
game.mkdir(parents=True)
(game / "mods").mkdir()
(classes / "META-INF").mkdir(parents=True)
(game / "saves").mkdir()
shutil.copytree(args.template_game / "saves/Cloak Integration Test", game / "saves/Cloak Integration Test")
shutil.copytree(args.template_game / "config", game / "config")
shutil.copy2(args.template_game / "options.txt", game)
shutil.copy2(args.mod_jar, game / "mods/crysis-legacy-port.jar")
for mod in [args.sodium, *args.extra_mod]:
    shutil.copy2(mod, game / "mods")
if args.shader_pack:
    (game / "shaderpacks").mkdir()
    shutil.copy2(args.shader_pack, game / "shaderpacks")
    (game / "config/iris.properties").write_text(
        f"enableShaders=true\nshaderPack={args.shader_pack.name}\n", encoding="utf-8")
(classes / "META-INF/neoforge.mods.toml").write_text(
    'modLoader="javafml"\nloaderVersion="[4,)"\nlicense="Local QA"\n'
    f'[[mods]]\nmodId="{args.probe_mod_id}"\nversion="1.0"\n'
    'displayName="Crysis mode overlay visual QA"\n', encoding="utf-8")
classpath = os.pathsep.join((project / "build/legacy-functional-qa/classpath.txt").read_text().splitlines())
merged = project / "build/moddev/artifacts/neoforge-21.1.265-merged.jar"
suffix = ".exe" if os.name == "nt" else ""
compiler_home = args.compiler_home or args.java_home
subprocess.run([str(compiler_home / f"bin/javac{suffix}"), "-proc:none", "-encoding", "UTF-8",
    "-sourcepath", str(classes), "-cp", os.pathsep.join([str(args.mod_jar.resolve()), classpath, str(merged)]),
    "-d", str(classes), str(args.probe)], check=True)
subprocess.run([str(compiler_home / f"bin/jar{suffix}"), "--create", "--file",
    str(game / "mods/mode-overlay-qa.jar"), "-C", str(classes), "."], check=True)
vm = (project / "build/moddev/clientRunVmArgs.txt").read_text()
program = (project / "build/legacy-functional-qa/program.txt").read_text()
if args.width and args.height:
    program += f'\n--width\n{args.width}\n--height\n{args.height}\n'
arguments = run / "java.args"
arguments.write_text('-Xmx3G\n-Dfile.encoding=UTF-8\n' + vm + '\n' + '\n'.join(args.jvm_arg) + '\n-cp\n"' +
    classpath.replace("\\", "/") + '"\nnet.neoforged.devlaunch.Main\n' + program, encoding="utf-8")
(run / "input-sha256.txt").write_text(hashlib.sha256(args.mod_jar.read_bytes()).hexdigest(), encoding="ascii")
java = args.java_home / ("bin/javaw.exe" if os.name == "nt" else "bin/java")
with (run / "console.log").open("w") as stdout, (run / "stderr.log").open("w") as stderr:
    process = subprocess.Popen([str(java), f"@{arguments}"], cwd=game, stdout=stdout, stderr=stderr)
(run / "client.pid").write_text(str(process.pid), encoding="ascii")
print(f"CLIENT_PID={process.pid}\nPROOF_DIRECTORY={run}")
