import sys

with open("app/src/main/java/com/example/ui/MainApp.kt", "r", encoding="utf-8") as f:
    code = f.read()

i = 0
n = len(code)
stack = []
line = 1
col = 1

while i < n:
    ch = code[i]
    if ch == "\n":
        line += 1
        col = 1
        i += 1
        continue
    
    # Check comments
    if ch == "/" and i + 1 < n:
        if code[i+1] == "/":
            while i < n and code[i] != "\n":
                i += 1
            continue
        elif code[i+1] == "*":
            i += 2
            while i + 1 < n and not (code[i] == "*" and code[i+1] == "/"):
                if code[i] == "\n":
                    line += 1
                i += 1
            i += 2
            continue
            
    # Check raw string """
    if ch == "\"" and i + 2 < n and code[i:i+3] == '"""':
        i += 3
        while i + 2 < n and not (code[i:i+3] == '"""'):
            if code[i] == "\n":
                line += 1
            i += 1
        i += 3
        continue
        
    # Check regular string "
    if ch == '"':
        i += 1
        while i < n and code[i] != '"':
            if code[i] == "\\":
                i += 2
                continue
            if code[i] == "\n":
                line += 1
            i += 1
        if i < n and code[i] == '"':
            i += 1
        continue

    # Check char literal
    if ch == "'":
        i += 1
        while i < n and code[i] != "'":
            if code[i] == "\\":
                i += 2
                continue
            i += 1
        if i < n and code[i] == "'":
            i += 1
        continue
        
    if ch == "{":
        snippet = code[max(0, i-30):min(n, i+30)].replace("\n", " ")
        stack.append((line, col, snippet))
    elif ch == "}":
        if stack:
            popped = stack.pop()
        else:
            print(f"Extra closing brace at line {line}, col {col}")
            
    col += 1
    i += 1

print(f"Unclosed braces count: {len(stack)}")
for s in stack:
    print(f"Line {s[0]}, Col {s[1]}: {s[2]}")
