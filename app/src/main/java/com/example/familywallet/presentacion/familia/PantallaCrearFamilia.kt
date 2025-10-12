import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.familia.FamiliaVMFactory
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import kotlinx.coroutines.launch

@Composable
fun PantallaCrearFamilia(
    vm: FamiliaViewModel,
    onHecho: (String) -> Unit,
    onAtras: () -> Unit
) {
    var nombreFamilia by remember { mutableStateOf("") }
    var alias         by remember { mutableStateOf("") }
    var error         by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = nombreFamilia,
            onValueChange = { nombreFamilia = it },
            label = { Text("Nombre familia") },
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Tu alias") },
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = nombreFamilia.isNotBlank() && alias.isNotBlank(),
            onClick = {
                scope.launch {
                    try {
                        val id = vm.crearFamilia(nombreFamilia, alias)   // ← AQUÍ
                        onHecho(id)
                    } catch (e: Exception) {
                        error = e.message ?: "Error al crear familia"
                    }
                }
            }
        ) { Text("Crear") }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}











