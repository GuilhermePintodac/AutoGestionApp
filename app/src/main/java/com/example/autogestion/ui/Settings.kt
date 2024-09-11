package com.example.autogestion.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.autogestion.data.AppDatabase
import java.util.zip.ZipInputStream

// Déclarer le launcher en tant que variable globale dans ton activité ou fragment
private lateinit var openFileLauncher: ActivityResultLauncher<String>


class Settings: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        openFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                restoreDatabase(this, uri)
            }
        }

        enableEdgeToEdge()
        setContent{
            SettingsScreen(
                onBackupClick = {
                    backupData(this)
                },
                onLoadClick = {
                    openFilePicker()
                }
            )
        }
    }
}

@Composable
fun SettingsScreen(onBackupClick: () -> Unit, onLoadClick: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Paramètres", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))

            // Bouton de sauvegarde
            Button(onClick = onBackupClick, modifier = Modifier.padding(8.dp)) {
                Text(text = "Sauvegarder la base de données")
            }

            // Bouton de chargement
            Button(onClick = onLoadClick, modifier = Modifier.padding(8.dp)) {
                Text(text = "Charger la base de données")
            }
        }
    }
}


fun backupData(context: Context) {
    // Créer un dossier "Backup" dans le répertoire externe de l'application
//    val backupDir = File(context.getExternalFilesDir(null)!!, "Backup")
    val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "AutoGestion Backup")
    if (!backupDir.exists()) {
        backupDir.mkdirs()  // Crée le dossier si nécessaire
    }

    // Créer le fichier ZIP dans ce dossier
//    val backupFile = File(backupDir, "backup.zip")
    val backupFile = createTimestampedBackupFile(backupDir)

    val databasePath = context.getDatabasePath("autoGestion_database").absolutePath
    val carteGriseDir = File(context.filesDir, "carteGrise")
    val factureDir = File(context.filesDir, "facture")

    val db = AppDatabase.getDatabase(context)
    db.close()

    Log.d("Backup", "Database Path: ${context.getDatabasePath("autoGestion_database").absolutePath}")

    val allClients = db.clientDao().getAllClients()
    Log.d("Backup", "All Clients: ${allClients}")



    try {
        // Ouvrir un fichier ZIP pour l'écriture
        val zipOutputStream = ZipOutputStream(FileOutputStream(backupFile))

        // Sauvegarder la base de données
        addFileToZip(File(databasePath), "autoGestion_database.db", zipOutputStream)

        // Sauvegarder les fichiers du dossier carteGrise
        carteGriseDir.listFiles()?.forEach { file ->
            addFileToZip(file, "carteGrise/${file.name}", zipOutputStream)
        }

        // Sauvegarder les fichiers du dossier facture
        factureDir.listFiles()?.forEach { file ->
            addFileToZip(file, "facture/${file.name}", zipOutputStream)
        }

        zipOutputStream.close()
        Log.d("Backup", "Backup successful: ${backupFile.absolutePath}")
    } catch (e: IOException) {
        Log.e("Backup", "Backup failed", e)
    }
}

fun addFileToZip(file: File, zipEntryName: String, zipOutputStream: ZipOutputStream) {
    val fileInputStream = FileInputStream(file)
    zipOutputStream.putNextEntry(ZipEntry(zipEntryName))
    fileInputStream.copyTo(zipOutputStream)
    zipOutputStream.closeEntry()
    fileInputStream.close()
}


//fun backupDatabase() {
//    val databasePath = getDatabasePath("your_database_name").absolutePath
//    val backupPath = File(externalCacheDir, "backup_database.db")
//
//    try {
//        File(databasePath).copyTo(backupPath, overwrite = true)
//        Log.d("Backup", "Backup successful: ${backupPath.absolutePath}")
//    } catch (e: IOException) {
//        Log.e("Backup", "Backup failed", e)
//    }
//}


//fun restoreDatabase(context: Context) {
//    val backupPath = File(context.externalCacheDir, "backup_database.db")
//    val databasePath = context.getDatabasePath("your_database_name").absolutePath
//
//    try {
//        backupPath.copyTo(File(databasePath), overwrite = true)
//        Log.d("Restore", "Database restored successfully.")
//    } catch (e: IOException) {
//        Log.e("Restore", "Database restore failed", e)
//    }
//}

//private fun restoreDatabase(uri: Uri, context: Context) {
//    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
//    val backupFile = File(context.getDatabasePath("autoGestion_database").absolutePath)
//
//    try {
//        inputStream?.use { input ->
//            FileOutputStream(backupFile).use { output ->
//                input.copyTo(output)
//            }
//        }
//        Log.d("Restore", "Database restored successfully")
//    } catch (e: IOException) {
//        Log.e("Restore", "Failed to restore database", e)
//    }
//}

private fun restoreDatabase(context: Context, zipUri: Uri) {
    // Étape 1 : Fermer la base de données si elle est ouverte
    val db = AppDatabase.getDatabase(context)
    db.close()

    // Étape 2 : Copier le fichier ZIP dans un fichier temporaire
    val inputStream: InputStream? = context.contentResolver.openInputStream(zipUri)
    val tempFile = File(context.cacheDir, "temp_backup.zip")

    try {
        // Copier le fichier ZIP dans un fichier temporaire
        FileOutputStream(tempFile).use { output ->
            inputStream?.copyTo(output)
        }

        // Étape 3 : Décompresser le fichier ZIP
        val filesDir = context.filesDir
        unzipFile(tempFile, filesDir)

        // Étape 4 : Remplacer le fichier de la base de données
        val databasePath = context.getDatabasePath("autoGestion_database").absolutePath
        val backupDatabaseFile = File(filesDir, "autoGestion_database.db")

        if (backupDatabaseFile.exists()) {
            // Supprimer l'ancienne base de données
            val databaseFile = File(databasePath)
            if (databaseFile.exists()) {
                databaseFile.delete()
            }

            // Copier le fichier de la base de données depuis le backup
            backupDatabaseFile.copyTo(File(databasePath), overwrite = true)
            Log.d("Restore", "Database restored successfully from $databasePath")
        } else {
            Log.e("Restore", "Backup database file not found in the ZIP")
        }

    } catch (e: IOException) {
        Log.e("Restore", "Failed to restore database", e)
    } finally {
        // Supprimer le fichier ZIP temporaire
        tempFile.delete()
    }
}

private fun unzipFile(zipFile: File, targetDir: File) {
    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val outputFile = File(targetDir, zipEntry.name)
            Log.d("Unzip", "Extracting ${outputFile.absolutePath}")  // Ajoutez cette ligne pour logguer chaque fichier extrait
            if (zipEntry.isDirectory) {
                outputFile.mkdirs()
            } else {
                // Créer le fichier et écrire le contenu décompressé
                outputFile.outputStream().use { output ->
                    zipInputStream.copyTo(output)
                }
            }
            zipInputStream.closeEntry()
            zipEntry = zipInputStream.nextEntry
        }
    }
}


fun openFilePicker() {
    openFileLauncher.launch("application/zip")
}



fun createTimestampedBackupFile(backupDir: File): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return File(backupDir, "backup_$timestamp.zip")
}

