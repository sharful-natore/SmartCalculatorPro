import re

with open('app/src/main/java/com/example/ui/screens/SpecialToolsScreen.kt', 'r') as f:
    content = f.read()

old_code = """            ToolType.TIP -> TipCalculatorCard(viewModel, themeColors)
            ToolType.ELECTRICITY_BILL -> ElectricityBillCalculatorCard(viewModel, themeColors)"""

new_code = """            ToolType.TIP -> TipCalculatorCard(viewModel, themeColors)
            ToolType.TEXT_COUNTER -> TextCounterCard(viewModel, themeColors)
            ToolType.PASSWORD_GENERATOR -> PasswordGeneratorCard(viewModel, themeColors)
            ToolType.ELECTRICITY_BILL -> ElectricityBillCalculatorCard(viewModel, themeColors)"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/screens/SpecialToolsScreen.kt', 'w') as f:
    f.write(content)
print("Updated SpecialToolsScreen.kt")
