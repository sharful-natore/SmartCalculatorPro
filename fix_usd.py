import re

with open("app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt", "r") as f:
    content = f.read()

target = """                    normalized.contains("taka") || normalized.contains("টাকা") || normalized.contains("bdt") -> {
                        if (normalized.contains("usd") || normalized.contains("dollar") || normalized.contains("ডলার")) {
                            fromU = "BDT - Bangladeshi Taka"
                            toU = "USD - US Dollar"
                            val usdRate = exchangeRates["BDT - Bangladeshi Taka"] ?: 120.0
                            rate = 1.0 / usdRate
                        }
                    }"""

replacement = """                    normalized.contains("taka") || normalized.contains("টাকা") || normalized.contains("bdt") -> {
                        if (normalized.contains("usd") || normalized.contains("dollar") || normalized.contains("ডলার")) {
                            val takaPos = maxOf(normalized.indexOf("taka"), normalized.indexOf("টাকা"), normalized.indexOf("bdt"))
                            val dollarPos = maxOf(normalized.indexOf("usd"), normalized.indexOf("dollar"), normalized.indexOf("ডলার"))
                            
                            if (takaPos < dollarPos && takaPos != -1) {
                                fromU = "BDT - Bangladeshi Taka"
                                toU = "USD - US Dollar"
                                val usdRate = exchangeRates["BDT - Bangladeshi Taka"] ?: 120.0
                                rate = 1.0 / usdRate
                            } else {
                                fromU = "USD - US Dollar"
                                toU = "BDT - Bangladeshi Taka"
                                rate = exchangeRates["BDT - Bangladeshi Taka"] ?: 120.0
                            }
                        }
                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt", "w") as f:
        f.write(content)
    print("Fixed!")
else:
    print("Target not found.")
