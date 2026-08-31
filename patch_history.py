import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target = """    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}"""

replacement = """    } catch (e: Exception) {
        e.printStackTrace()
    }
    val validList = list.filter { java.io.File(it.filePath).exists() }
    if (validList.size != list.size) {
        saveCvHistory(context, validList)
    }
    return validList
}"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success history")
else:
    print("Target not found for history")
