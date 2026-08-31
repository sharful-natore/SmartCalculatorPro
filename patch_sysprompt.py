import re

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

old_sys_prompt = r'"You are an expert HR Manager and professional resume writer. Write ONLY the precise content requested \(such as the career objective, skills, or experience description\). You MUST NOT include any conversational introduction, greetings, wrap-up remarks, explanation, chat messages, markdown headers, or markdown block quotes \(such as ```\). Your entire response must be ONLY the raw, copy-pasteable content block to be directly inserted into the resume."'
new_sys_prompt = r'"CRITICAL INSTRUCTION: You are an expert HR Manager and professional resume writer. Write ONLY the exact, precise text content requested for the CV field. DO NOT include any conversational text, greetings, explanations, or wrap-up remarks. DO NOT include introductory phrases like \'Here is the objective:\' or \'Certainly!\'. DO NOT use markdown code blocks (```). Your ENTIRE output must be exclusively the raw text to be inserted into the CV, ready to copy-paste. Nothing else."'

content = re.sub(old_sys_prompt, new_sys_prompt, content)

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write(content)
