package com.example.autogestion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavBar(text : String,onBackClick: () -> Unit){
    // Up Bar
    Row(modifier = Modifier
        .height(56.dp)
        .fillMaxWidth()
        .background(Color(0xFFF3EDF7)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Button to go back
        IconButton(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
        }
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

    }
}

@Composable
fun NavBarClients(text : String,onBackClick: () -> Unit, onContactClick: () -> Unit){
    // Up Bar
    Row(modifier = Modifier
        .height(56.dp)
        .fillMaxWidth()
        .background(Color(0xFFF3EDF7)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Button to go back
        IconButton(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
        }
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.weight(1f))
        
        IconButton(onClick = onContactClick, modifier = Modifier.padding(8.dp)) {
            Icon(imageVector = Icons.Default.Contacts,
                contentDescription = "Selectionner un Contact" )
        }

    }
}