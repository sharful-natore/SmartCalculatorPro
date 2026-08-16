import re

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Instead of doing direct string replace which is tricky with python escaping, let's just use regex to replace newlines inside the quotes.
# We will find all double-quoted strings and replace newlines inside them with \n

def replacer(match):
    s = match.group(0)
    # Only replace literal newlines with \n
    return s.replace('\n', '\\n')

content = re.sub(r'"[^"]+"', replacer, content, flags=re.MULTILINE | re.DOTALL)

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "w", encoding="utf-8") as f:
    f.write(content)

