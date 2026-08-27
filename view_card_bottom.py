with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r") as f:
    lines = f.readlines()

start = -1
for i, line in enumerate(lines):
    if "fun HadithReaderCardItem(" in line:
        start = i
        break

if start != -1:
    print("".join(lines[start+70:start+180]))
