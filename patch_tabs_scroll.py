import re

def update_tab_scroll(tab_name):
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
        content = f.read()

    # Find the function signature
    sig_pattern = r"(private fun " + tab_name + r"\([\s\S]*?)(\) \{)"
    match = re.search(sig_pattern, content)
    if match:
        sig = match.group(1)
        if "isScrollable: Boolean" not in sig:
            new_sig = sig + ",\n    isScrollable: Boolean = true" + match.group(2)
            content = content.replace(match.group(0), new_sig)
            
            # Find the modifier
            mod_pattern = r"(\.fillMaxSize\(\)\s*)\.verticalScroll\([^)]*\)"
            
            # To be safe, we only replace the first occurrence after the function signature.
            idx = content.find("fun " + tab_name)
            sub = content[idx:idx+1000]
            if ".verticalScroll" in sub:
                new_sub = re.sub(mod_pattern, r"\1.let { if (isScrollable) it.verticalScroll(scrollState) else it }", sub)
                content = content[:idx] + new_sub + content[idx+1000:]
                
            with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
                f.write(content)
            print(f"Updated {tab_name}")
        else:
            print(f"{tab_name} already has isScrollable")
    else:
        print(f"Could not find signature for {tab_name}")

update_tab_scroll("ProfileAndPersonasTab")
update_tab_scroll("ExperienceTab")
update_tab_scroll("EducationAndSkillsTab")

