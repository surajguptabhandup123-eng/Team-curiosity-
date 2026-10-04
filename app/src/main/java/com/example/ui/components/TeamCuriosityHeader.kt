package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TechCardDark
import com.example.ui.theme.TechSlateDark

@Composable
fun TeamCuriosityHeader(
    currentRole: UserRole,
    studentName: String? = null,
    onOpenSettingsClick: (() -> Unit)? = null,
    onSwitchRoleClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TechCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(CyanPrimary.copy(alpha = 0.5f), Color.Transparent, CyanPrimary.copy(alpha = 0.3f))
            ),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Official Team Curiosity circular logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, CyanGlow, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.team_curiosity_logo),
                    contentDescription = "Team Curiosity Curious City FTC Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TEAM CURIOSITY",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = CyanPrimary
                )
                Text(
                    text = "CURIOUS CITY FTC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = Color(0xFFBAE6FD)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentRole == UserRole.ADMIN_MENTOR)
                        Color(0xFF0284C7).copy(alpha = 0.25f)
                    else
                        Color(0xFF10B981).copy(alpha = 0.25f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (currentRole == UserRole.ADMIN_MENTOR) CyanPrimary else Color(0xFF10B981),
                                Color.Transparent
                            )
                        ),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentRole == UserRole.ADMIN_MENTOR) Icons.Default.Lock else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (currentRole == UserRole.ADMIN_MENTOR) CyanPrimary else Color(0xFF34D399),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentRole == UserRole.ADMIN_MENTOR) "ADMIN / MENTOR" else (studentName ?: "STUDENT"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentRole == UserRole.ADMIN_MENTOR) CyanPrimary else Color(0xFF34D399)
                        )
                    }
                }
            }

            // Admin Settings button (if admin)
            if (currentRole == UserRole.ADMIN_MENTOR && onOpenSettingsClick != null) {
                IconButton(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, CyanPrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Switch role button
            IconButton(
                onClick = onSwitchRoleClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch Role",
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
