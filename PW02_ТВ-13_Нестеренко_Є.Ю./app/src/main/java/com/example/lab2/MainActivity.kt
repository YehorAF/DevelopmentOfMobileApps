package com.example.lab2

import kotlin.math.pow
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
import com.example.lab2.ui.theme.Lab2Theme
import java.text.DecimalFormatSymbols

//const val EPS = 0.01

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

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    Lab2Theme {
        val currentTask = remember { mutableStateOf("") }
        Column {
            for (task in arrayOf("task1", "task2", "task3")) {
                Row {
                    Text(task)
                    RadioButton(
                        selected = currentTask.value == task,
                        onClick = { currentTask.value = task }
                    )
                }
            }

            when (currentTask.value) {
                "task1" -> Task1View()
                "task2" -> Task2View()
                "task3" -> Task3View()
            }
        }
    }
}

@Composable
fun Task1View() {
    val massInput = remember{ mutableStateOf("0") }
    val cdafInput = remember { mutableStateOf("0") }
    val hdafInput = remember { mutableStateOf("0") }
    val sdafInput = remember { mutableStateOf("0") }
    val odafInput = remember { mutableStateOf("0") }
    val vdafInput = remember { mutableStateOf("0") }
    val qdafInput = remember { mutableStateOf("0") }
    val inputData = mapOf(
        "Mass" to massInput,
        "Cdaf" to cdafInput,
        "Hdaf" to hdafInput,
        "Sdaf" to sdafInput,
        "Odaf" to odafInput,
        "Vdaf" to vdafInput,
        "Qdaf" to qdafInput,
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
            val mass = toDoubleOrZero(massInput.value)
            val cdaf = toDoubleOrZero(cdafInput.value)
            val hdaf = toDoubleOrZero(hdafInput.value)
            val sdaf = toDoubleOrZero(sdafInput.value)
            val odaf = toDoubleOrZero(odafInput.value)
            val vdaf = toDoubleOrZero(vdafInput.value)
            val qdaf = toDoubleOrZero(qdafInput.value)

            // обчислюємо вологість
            val ww = (339 * cdaf + 1030 * hdaf - 108.8 * (odaf - sdaf) - qdaf * 1000) / 25
            alerts.value += "Ww: $ww\n"
            // обчислюємо зольність
            val aw = (vdaf - ww) * calculateCWM(ww)
            alerts.value += "Aw: $aw\n"
            // робоча теплота
            val qw = calculateQCW(qdaf, ww, aw)
            alerts.value += "Qw: $qw\n"
            val pei = calculatePEI(qw, 0.8, aw, 1.5, 0.985, 0.0)
            val gesp = calculateGESP(pei, qw, mass)

            alerts.value += "PEI: $pei\nGESP: $gesp"
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
    val massInput = remember{ mutableStateOf("0") }
    val adInput = remember{ mutableStateOf("0") }
    val wwInput = remember{ mutableStateOf("0") }
    val qdafInput = remember{ mutableStateOf("0") }
    val inputData = mapOf(
        "Mass" to massInput,
        "Ad" to adInput,
        "Ww" to wwInput,
        "Qdaf" to qdafInput,
    )
    val alerts = remember {
        mutableStateOf("")
    }
    Column {
        inputData.forEach {
            DecimalInput(label = it.key, value = it.value)
        }
        Button(onClick = {
            val mass = toDoubleOrZero(massInput.value)
            val ad = toDoubleOrZero(adInput.value)
            val ww = toDoubleOrZero(wwInput.value)
            val qdaf = toDoubleOrZero(qdafInput.value)

            // робоча зольність
            val aw = ad * calculateCWM(ww)
            val qw = calculateQCW(qdaf, ww, aw)
            val pei = calculatePEI(qw, 1.0, aw, 0.0, 0.985, 0.0)
            val gesp = calculateGESP(pei, qw, mass)

            alerts.value += "PEI: $pei\nGESP: $gesp"
        }) {
            Text("Calculate")
        }
        Row {
            Text(text = alerts.value)
        }
    }
}

@Composable
fun Task3View() {
    val massInput = remember{ mutableStateOf("0") }
    val pInput = remember{ mutableStateOf("0") }
    val qdInput = remember{ mutableStateOf("0") }
    val ch4Input = remember{ mutableStateOf("0") }
    val c2h6Input = remember{ mutableStateOf("0") }
    val c3h8Input = remember{ mutableStateOf("0") }
    val c4h10Input = remember{ mutableStateOf("0") }
    val co2Input = remember{ mutableStateOf("0") }
//    val n2Input = remember{ mutableStateOf("0") }
    val inputData = mapOf(
        "Mass" to massInput,
        "p" to pInput,
        "Qd" to qdInput,
        "CH4" to ch4Input,
        "C2H6" to c2h6Input,
        "C3H8" to c3h8Input,
        "C4H10" to c4h10Input,
        "CO2" to co2Input,
//        "N2" to n2Input,
    )
    val alerts = remember {
        mutableStateOf("")
    }
    Column {
        inputData.forEach {
            DecimalInput(label = it.key, value = it.value)
        }
        Button(onClick = {
            val mass = toDoubleOrZero(massInput.value)
            val pn = toDoubleOrZero(pInput.value)
            val qd = toDoubleOrZero(qdInput.value)
            val ch4 = toDoubleOrZero(ch4Input.value)
            val c2h6 = toDoubleOrZero(c2h6Input.value)
            val c3h8 = toDoubleOrZero(c3h8Input.value)
            val c4h10 = toDoubleOrZero(c4h10Input.value)
            val co2 = toDoubleOrZero(co2Input.value)
            // обчислюємо масу
            val mch4 = 0.716 * 0.001 * ch4
            val mc2h6 = 1.342 * 0.001 * c2h6
            val mc3h8 = 1.967 * 0.001 * c3h8
            val mc4h10 = 2.593 * 0.001 * c4h10
            val mco2 = 1.964 * 0.001 * co2
            // обчислємо вміст кожного елементу
            val cdaf = calculateElementDaf(
                calculateElementMass(mch4, 1, 4, 1),
                calculateElementMass(mc2h6, 2, 6, 2),
                calculateElementMass(mc3h8, 3, 8, 3),
                calculateElementMass(mc4h10, 4, 10, 4),
                0.273 * mco2
            )
            // робоча теплота
            val qw = qd / pn
            // масова витрата
            val b = mass * pn
            val knox = 68.1
            val kco = 17.0
            val kco2 = (44.0 / 12) * (cdaf / 100) * (10.0.pow(6) / qw) * 0.995
            val khg = 0.0001
            val kn2o = 0.1
            val kch4 = 1.0
            val enox = calculateGESP(knox, qw, b)
            val eco = calculateGESP(kco, qw, b)
            val eco2 = calculateGESP(kco2, qw, b)
            val ehg = calculateGESP(khg, qw, b)
            val en2o = calculateGESP(kn2o, qw, b)
            val ech4 = calculateGESP(kch4, qw, b)
            val esum = enox + eco + eco2 + ehg + en2o + ech4

            alerts.value = """
                k NOx: $knox
                k CO: $kco
                k CO2: $kco2
                k Hg: $khg
                k N2O: $kn2o
                k CH4: $kch4
                Enox: $enox
                Eco: $eco
                Eco2: $eco2
                Ehg: $ehg
                En2o: $en2o
                Ech4: $ech4
                Esum: $esum
                
                
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

// робоча суха маса
//fun calculateWDM(ww: Double) : Double {
//    return 100 / (100 - ww)
//}

// робоча горюча маса
//fun calculateWCM(ww: Double, aw: Double) : Double {
//    return 100 / (100 - ww - aw)
//}

// перехід з горючої на робочу
fun calculateCWM(ww: Double, aw: Double = 0.0) : Double {
    return (100 - ww - aw) / 100
}

// маса
//fun calculateMass(el: Double, coeff: Double) : Double {
//    return  el * coeff
//}

// нижча теплота згорання
//fun calculateQWL(cw: Double, hw: Double, ow: Double, sw: Double, ww: Double) : Double {
//    return (339 * cw + 1030 * hw - 108.8 * (ow - sw) - 25 * ww) / 1000
//}

// обчислення нижчої теплоти згорання через робочу
//fun calculateQFromW(qwl: Double, ww: Double, coeff: Double) : Double {
//    return (qwl + 0.025 * ww) * coeff
//}

// теплота згорання з горючої маси на робочу
fun calculateQCW(qdaf: Double, ww: Double, aw: Double) : Double {
    return qdaf * (100 - ww - aw) / 100 - 0.025 * ww
}

// показник емісії твердих частинок
fun calculatePEI(
    qw: Double,
    atrash: Double,
    aw: Double,
    ctrash: Double,
    ec: Double,
    pr: Double
) : Double {
    return (10.0.pow(6) / qw) * atrash * (aw / (100 - ctrash)) * (1 - ec) + pr
}

// валовий викид
fun calculateGESP(pei: Double, qw: Double, cost: Double) : Double {
    return 10.0.pow(-6) * pei * qw * cost
}

// маса елементу
fun calculateElementMass(mass: Double, p: Int, q: Int, pt: Int) : Double {
    return ((12.0 * pt) / (12.0 * p + q)) * mass
}

// сума для обчислення маси
fun calculateElementDaf(pn: Double, vararg els: Double) : Double {
    return (100 / pn) * els.sum()
}