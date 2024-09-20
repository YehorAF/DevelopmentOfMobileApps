package com.example.lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lab1.ui.theme.Lab1Theme
import java.text.DecimalFormatSymbols

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                MainPreview()
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

data class Element(val name: String, val value: Double)

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    Lab1Theme {
        val currentTask = remember {
            mutableStateOf("")
        }
        Column {
            for (task in arrayOf("task1", "task2")) {
                Row {
                    Text(task)
                    RadioButton(
                        selected = currentTask.value == task,
                        onClick = { currentTask.value = task }
                    )
                }
            }

            if (currentTask.value == "task1") {
                Task1View()
            } else if (currentTask.value == "task2") {
                Task2View()
            }
        }
    }
}

@Composable
fun Task1View() {
    val eps = 0.01
    val hwInput = remember { mutableStateOf("0") }
    val cwInput = remember { mutableStateOf("0") }
    val swInput = remember { mutableStateOf("0") }
    val nwInput = remember { mutableStateOf("0") }
    val owInput = remember { mutableStateOf("0") }
    val wwInput = remember { mutableStateOf("0") }
    val awInput = remember { mutableStateOf("0") }
    val inputData = mapOf(
        "Hydrogen" to hwInput,
        "Carbon" to cwInput,
        "Sulfur" to swInput,
        "Nitrogen" to nwInput,
        "Oxygen" to owInput,
        "Ww" to wwInput,
        "Aw" to awInput,
    )
    val alerts = remember {
        mutableStateOf("")
    }
    Column {
        inputData.forEach {
            DecimalInput(label = it.key, value = it.value)
        }
        Button(onClick = {
            alerts.value = ""
            val workMassList = inputData.map {
                it.key to toDoubleOrZero(it.value.value)
            }
            val workMassMap = workMassList.toMap()
            var sum = workMassList.sumOf { it.second }
            if (sum < 100 - eps || sum > 100 + eps) {
                alerts.value += "Error: sum of elements must be 100 but $sum"
                return@Button
            }
            val ww = workMassMap.getValue("Ww")
            val aw = workMassMap.getValue("Aw")
            val wdm = calculateWDM(ww)
            val wcm = calculateWCM(ww, aw)

            alerts.value += "WDM = $wdm\nWCM = $wcm\n"

            val dryMass = workMassList.filter { el -> el.first != "Ww" }.map{ el ->
                val d = calculateMass(el.second, wdm)
                alerts.value += "Dry ${el.first} = $d\n"
                Element(el.first, d)
            }
            sum = dryMass.sumOf { it.value }
            alerts.value += "Sum: $sum\n"
            if (sum < 100 - eps || sum > 100 + eps) {
                alerts.value += "Error: sum of elements must be equal 100 but $sum"
                return@Button
            }

            val combustibleMass = workMassList.filter { el ->
                el.first != "Ww" && el.first != "Aw"
            }.map { el ->
                val d = calculateMass(el.second, wcm)
                alerts.value += "Combustible ${el.first} = $d\n"
                Element(el.first, d)
            }
            sum = combustibleMass.sumOf { it.value }
            alerts.value += "Sum: $sum\n"
            if (sum < 100 - eps || sum > 100 + eps) {
                alerts.value = "Error: sum of elements must be equal 100%"
                return@Button
            }

            val qwl = calculateQWL(
                workMassMap.getValue("Carbon"),
                workMassMap.getValue("Hydrogen"),
                workMassMap.getValue("Oxygen"),
                workMassMap.getValue("Sulfur"),
                ww
            )
            val qdm = calculateQFromW(qwl, ww, wdm)
            val qcm = calculateQFromW(qwl, ww, wcm)

            alerts.value += "QW = $qwl\nQDM = $qdm\nQCM = $qcm"
        }) {
            Text("Calculate")
        }
        Row {
            Text(text = alerts.value)
        }
    }
}

@Composable
fun Task2View() {
    val eps = 0.01
    val alerts = remember { mutableStateOf("") }
    val carbonInput = remember { mutableStateOf("0") }
    val hydrogenInput = remember { mutableStateOf("0") }
    val oxygenInput = remember { mutableStateOf("0") }
    val sulfurInput = remember { mutableStateOf("0") }
    val nitrogenInput = remember { mutableStateOf("0") }
    val qdafInput = remember { mutableStateOf("0") }
    val wwInput = remember { mutableStateOf("0") }
    val awInput = remember { mutableStateOf("0") }
    val vanadiumInput = remember { mutableStateOf("0") }
    val inputData = mapOf(
        "Carbon" to carbonInput,
        "Hydrogen" to hydrogenInput,
        "Oxygen" to oxygenInput,
        "Sulfur" to sulfurInput,
        "Nitrogen" to nitrogenInput,
        "Qdaf" to qdafInput,
        "Ww" to wwInput,
        "Aw" to awInput,
        "Vanadium" to vanadiumInput
    )
    val notInData = arrayOf("Qdaf", "Ww", "Aw", "Vanadium")
    Column {
        inputData.forEach{ DecimalInput(label = it.key, value = it.value)}
        Button(onClick = {
            alerts.value = ""
            var workMass = inputData
                .filter { it.key !in notInData }
                .map { it.key to toDoubleOrZero(it.value.value) }
            val qdaf = toDoubleOrZero(qdafInput.value)
            val ww = toDoubleOrZero(wwInput.value)
            var aw = toDoubleOrZero(awInput.value)
            var vanadium = toDoubleOrZero(vanadiumInput.value)
            val cwm2 = calculateCWM(ww)
            aw = calculateMass(aw, cwm2)
            val cwm1 = calculateCWM(ww, aw)

            workMass = workMass.map {
//                it.first to calculateWM(it.second, wr, argon)
                it.first to calculateMass(it.second, cwm1)
            }
//            vanadium = calculateWM(vanadium, wr, argon)
//            argon = calculateWM(argon, wr, 0.0)
            vanadium = calculateMass(vanadium, cwm2)
            val sum = workMass.sumOf { it.second } + ww + aw
            if (sum < 100 - eps || sum > 100 + eps) {
                alerts.value = "Error: sum of elements must be equal 100 but $sum"
                return@Button
            }
            val qr = calculateQCW(qdaf, ww, aw)
            workMass.forEach {
                alerts.value += "Workable ${it.first} = ${it.second}\n"
            }
            alerts.value += "Aw = $aw\n"
            alerts.value += "Workable Vanadium = $vanadium\n"
            alerts.value += "Qr = $qr"
        }) {
            Text("Calculate")
        }
        Text(text = alerts.value)
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

// робоча суха маса
fun calculateWDM(ww: Double) : Double {
    return 100 / (100 - ww)
}

// робоча горюча маса
fun calculateWCM(ww: Double, aw: Double) : Double {
    return 100 / (100 - ww - aw)
}

// перехід з горючої на робочу
fun calculateCWM(ww: Double, aw: Double = 0.0) : Double {
    return (100 - ww - aw) / 100
}

// маса
fun calculateMass(el: Double, coeff: Double) : Double {
    return  el * coeff
}

// нижча теплота згорання
fun calculateQWL(cw: Double, hw: Double, ow: Double, sw: Double, ww: Double) : Double {
    return (339 * cw + 1030 * hw - 108.8 * (ow - sw) - 25 * ww) / 1000
}

// обчислення нижчої теплоти згорання через робочу
fun calculateQFromW(qwl: Double, ww: Double, coeff: Double) : Double {
    return (qwl + 0.025 * ww) * coeff
}

// теплота згорання з горючої маси на робочу
fun calculateQCW(qdaf: Double, ww: Double, aw: Double) : Double {
    return qdaf * (100 - ww - aw) / 100 - 0.025 * ww
}