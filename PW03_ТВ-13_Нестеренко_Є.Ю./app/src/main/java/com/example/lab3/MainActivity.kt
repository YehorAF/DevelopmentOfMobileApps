package com.example.lab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.lab3.ui.theme.Lab3Theme
import java.text.DecimalFormatSymbols
import java.util.Vector
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

const val STEP = 0.00001

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab3Theme {
                Task1View()
            }
        }
    }
}

class DecimalFormatter(
    symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()
) {
    private val decimalSeparator = symbols.decimalSeparator

    fun cleanup(input: String): String {

        if (input.matches("\\D".toRegex())) return ""
        if (input.matches("0+".toRegex())) return "0"

        val sb = StringBuilder()

        var hasDecimalSep = false

        for (char in input) {
            if (char.isDigit()) {
                sb.append(char)
                continue
            }
            if (char == decimalSeparator && !hasDecimalSep && sb.isNotEmpty()) {
                sb.append(char)
                hasDecimalSep = true
            }
        }

        return sb.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun Task1View() {
    val pcInput = remember { mutableStateOf("0") }
    val p1Input = remember { mutableStateOf("0") }
    val p2Input = remember { mutableStateOf("0") }
    val s1Input = remember { mutableStateOf("0") }
    val s2Input = remember { mutableStateOf("0") }
    val costInput = remember { mutableStateOf("0") }
    val inputData = mapOf(
        "PC" to pcInput,
        "p1" to p1Input,
        "p2" to p2Input,
        "S1" to s1Input,
        "S2" to s2Input,
        "Cost per kWt" to costInput,
    )
    val alerts = remember {
        mutableStateOf("")
    }
    Column {
        inputData.forEach {
            DecimalInput(label = it.key, value = it.value)
        }
        Button(onClick = {
            val pc = toDoubleOrZero(pcInput.value)
            val p1 = toDoubleOrZero(p1Input.value)
            val p2 = toDoubleOrZero(p2Input.value)
            val s1 = toDoubleOrZero(s1Input.value)
            val s2 = toDoubleOrZero(s2Input.value)
            val cost = toDoubleOrZero(costInput.value)

            val dw1 = calculate(p1, p2, s1, pc, STEP)
            val w1 = 24 * pc * dw1
            val profit1 = w1 * cost
            val w2 = 24 * pc * (1 - dw1)
            val fine1 = w2 * cost
            val cleanProfit1 = profit1 - fine1

            val dw2 = calculate(p1, p2, s2, pc, STEP)
            val w3 = 24 * pc * dw2
            val profit2 = w3 * cost
            val w4 = 24 * pc * (1 - dw2)
            val fine2 = w4 * cost
            val cleanProfit2 = profit2 - fine2

            alerts.value = """
                dW1 = $dw1
                W1 = $w1
                W2 = $w2
                Profit = $profit1
                Fine = $fine1
                Clean = $cleanProfit1
                
                dW2 = $dw2
                W3 = $w3
                W4 = $w4
                Profit = $profit2
                Fine = $fine2
                Clean = $cleanProfit2
            """.trimIndent()
        }) {
            Text("Calculate")
        }
        Row {
            Text(text = alerts.value)
        }
    }
}

@Composable
fun DecimalInput(label: String, value: MutableState<String>) {
    val decimalFormatter = DecimalFormatter()
    OutlinedTextField(
        value = value.value,
        label = { Text(label) },
        onValueChange = {value.value = decimalFormatter.cleanup(it)},
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

fun toDoubleOrZero(s: String) : Double {
    if (s.isNotEmpty()) {
        return s.toDouble()
    }
    return  0.0
}

fun boolesRuleNode(
    f0: Double,
    f1: Double,
    f2: Double,
    f3: Double,
    f4: Double,
    step: Double
) : Double {
    return 2/45.0 * step * (7*f0 + 32*f1 + 12*f2 + 32*f3 + 7*f4)
}

fun formula(p: Double, s: Double, pc: Double) : Double {
    return exp(- (p - pc).pow(2) / (2 * s.pow(2))) / (s * sqrt(2 * PI))
}

fun calculate(p1: Double, p2: Double, s: Double, pc: Double, step: Double) : Double {
    val ps = Vector<Double>()
    var p = p1

    while (p <= p2) {
        ps.add(p)
        p += step
    }

    println("add ps in vector")

    var f0 = formula(ps[0], s, pc)
    var f1 = formula(ps[1], s, pc)
    var f2 = formula(ps[2], s, pc)
    var f3 = formula(ps[3], s, pc)
    var f4 = 0.0
    var sum = 0.0

    var i = 4
    while (i < ps.size) {
        f4 = formula(ps[i], s, pc)
        sum += boolesRuleNode(f0, f1, f2, f3, f4, step/4)
        f0 = f1
        f1 = f2
        f2 = f3
        f3 = f4
        i += 1
    }

    println("calculate sum")

    return  sum
}