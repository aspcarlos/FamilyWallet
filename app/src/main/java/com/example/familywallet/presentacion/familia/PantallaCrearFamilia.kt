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
    onHecho: (String) -> Unit,      // recibe el id creado
) {
    var nombreFamilia by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val vm: FamiliaViewModel = viewModel(
        factory = FamiliaVMFactory(
            familiaRepo = ServiceLocator.familiaRepo,
            authRepo = ServiceLocator.authRepo
        )
    )
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear familia", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombreFamilia,
            onValueChange = { nombreFamilia = it },
            label = { Text("Nombre de la familia") },
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Tu alias") },
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val id = vm.crearFamilia(
                            nombre = nombreFamilia,
                            aliasOwner = alias
                        )
                        onHecho(id)   // OK
                    } catch (e: Exception) {
                        error = e.message ?: "Error al crear familia"
                    }
                }
            },
            enabled = nombreFamilia.isNotBlank() && alias.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp)
        ) {
            Text("Crear")
        }
    }
}










