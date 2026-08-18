package com.resthalflab.resthalfapp.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhRadius
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhCard
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedCard

// Banner gradient (on-brand: sits around the indigo primary). Kept local to the profile hero.
private val BannerTop = Color(0xFF7B61FF)
private val BannerBottom = Color(0xFF5B3FE0)
private val ToolbarHeight = 56.dp

/**
 * Account/profile screen. Menu rows are clickable but inert for now (wiring lands later); only Sign
 * Out is live. A brand-gradient hero sits under a toolbar whose "My account" title + background fade
 * in as the hero scrolls away.
 */
@Composable
fun ProfileScreen(component: ProfileComponent) {
    val session by component.session.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val toolbarFraction by remember {
        derivedStateOf {
            val start = with(density) { 40.dp.toPx() }
            val end = with(density) { 150.dp.toPx() }
            ((scroll.value - start) / (end - start)).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll)) {
            ProfileHero(
                name = session?.displayName?.takeIf { it.isNotBlank() } ?: "Guest",
                onEditProfile = {},
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(RhSpacing.lg),
            ) {
                AccountSecurityCard(email = session?.email)
                PaymentSettingsCard()
                MemberProfileCard()
                PersonalInfoCard()
                LinkAccountsCard()
                SignOutCard(onSignOut = component::onLogoutClicked)
                DeleteAccountFooter()
                Spacer(Modifier.height(RhSpacing.md))
            }
        }

        ProfileToolbar(fraction = toolbarFraction, onBack = {})
    }
}

// ---- Hero -------------------------------------------------------------------------------------

@Composable
private fun ProfileHero(name: String, onEditProfile: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(BannerTop, BannerBottom))),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = RhSpacing.xl,
                    end = RhSpacing.xl,
                    top = RhSpacing.xl,
                    bottom = RhSpacing.xl,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(color = Color(0xFFFFCA28), shape = CircleShape, modifier = Modifier.size(64.dp)) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(RhSpacing.md),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = RhSpacing.lg)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Spacer(Modifier.height(RhSpacing.xs))
                Row(
                    modifier = Modifier.clickable(onClick = onEditProfile),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Edit profile detail",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.92f),
                    )
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileToolbar(fraction: Float, onBack: () -> Unit) {
    val contentColor = lerp(Color.White, MaterialTheme.colorScheme.onSurface, fraction)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = fraction)),
    ) {
//        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
//            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = contentColor)
//        }
        Text(
            text = "My account",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center).alpha(fraction),
        )
    }
}

// ---- Cards ------------------------------------------------------------------------------------

@Composable
private fun AccountSecurityCard(email: String?) {
    SectionCard(title = "Account Security") {
        SettingRow(
            icon = Icons.Outlined.Email,
            label = "Linked email",
            onClick = {},
            trailing = { ValueChevron(value = email?.let(::maskEmail) ?: "Add") },
        )
        RowDivider()
        SettingRow(
            icon = Icons.Outlined.Phone,
            label = "Linked Phone",
            onClick = {},
            trailing = { SmallPrimaryButton(text = "Link", onClick = {}) },
        )
        RowDivider()
        SettingRow(
            icon = Icons.Outlined.Lock,
            label = "Reset Password",
            onClick = {},
            trailing = { SmallPrimaryButton(text = "Set", onClick = {}) },
        )
        RowDivider()
        SettingRow(
            icon = Icons.Outlined.PhoneAndroid,
            label = "Manage devices",
            onClick = {},
            trailing = { Chevron() },
        )
        RowDivider()
        SettingRow(
            icon = Icons.Outlined.History,
            label = "Sign-in history",
            onClick = {},
            trailing = { Chevron() },
        )
    }
}

@Composable
private fun PaymentSettingsCard() {
    SectionCard(title = "Payment Settings") {
        SettingRow(
            icon = Icons.Outlined.Shield,
            label = "Security PIN Code",
            onClick = {},
            trailing = { ValueChevron(value = "Not set") },
        )
    }
}

@Composable
private fun MemberProfileCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RhRadius.card,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(modifier = Modifier.padding(RhSpacing.xl)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Member profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                Text(
                    text = "REWARDS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
            Spacer(Modifier.height(RhSpacing.sm))
            Text(
                text = "Add your legal name and date of birth to complete your member profile and be " +
                    "eligible for membership rewards or upgrades. Please make sure your info matches your travel ID.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(RhSpacing.lg))
            RhOutlinedCard(modifier = Modifier.fillMaxWidth().clickable {}) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Legal name",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Date of birth",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SmallPrimaryButton(text = "Edit", onClick = {})
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoCard() {
    SectionCard(
        title = "Personal Info",
        action = {
            Row(
                modifier = Modifier.clickable {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(RhSpacing.xs))
                Text(
                    "Edit",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) {
        Text(
            text = "Enter your personal info to help us tailor your trips and discover nearby deals for you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = RhSpacing.xs, bottom = RhSpacing.md),
        )
        RowDivider()
        SettingRow(Icons.Outlined.Person, "Display name", onClick = {}, trailing = { EmptyValue() })
        RowDivider()
        SettingRow(Icons.Outlined.People, "Gender", onClick = {}, trailing = { EmptyValue() })
        RowDivider()
        SettingRow(Icons.Outlined.Public, "Country or Region", onClick = {}, trailing = { EmptyValue() })
        RowDivider()
        SettingRow(Icons.Outlined.LocationCity, "City of Residence", onClick = {}, trailing = { EmptyValue() })
        RowDivider()
        SettingRow(Icons.Outlined.Place, "Frequently visited city", onClick = {}, trailing = { EmptyValue() })
    }
}

@Composable
private fun LinkAccountsCard() {
    SectionCard(
        title = "Link Accounts",
        action = {
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1FA855)) {
                Text(
                    "Easy sign-in",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = RhSpacing.sm, vertical = 3.dp),
                )
            }
        },
    ) {
        Text(
            text = "Linking a third-party account enables quick sign-in using your account information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = RhSpacing.xs, bottom = RhSpacing.md),
        )
        RowDivider()
        LinkRow("G", Color(0xFF4285F4), "Google") { SmallOutlinedButton(text = "Unlink", onClick = {}) }
        RowDivider()
        LinkRow("A", Color(0xFF111111), "Apple") { SmallPrimaryButton(text = "Link", onClick = {}) }
        RowDivider()
        LinkRow("f", Color(0xFF1877F2), "Facebook") { SmallPrimaryButton(text = "Link", onClick = {}) }
    }
}

@Composable
private fun SignOutCard(onSignOut: () -> Unit) {
    RhCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onSignOut), contentPadding = RhSpacing.lg) {
        Text(
            text = "Sign Out",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DeleteAccountFooter() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = RhSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
    ) {
        Text(
            text = "Delete My Account",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {},
        )
        Text(
            text = "Once deleted, all account information will be removed. You will not be able to recover this information.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ---- Building blocks --------------------------------------------------------------------------

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    RhCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
        Spacer(Modifier.height(RhSpacing.md))
        RowDivider()
        content()
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = RhSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(start = RhSpacing.md),
        )
        trailing()
    }
}

@Composable
private fun LinkRow(monogram: String, brandColor: Color, name: String, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(color = brandColor, shape = CircleShape, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = monogram,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(start = RhSpacing.md),
        )
        action()
    }
}

@Composable
private fun ValueChevron(value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(RhSpacing.xs))
        Chevron()
    }
}

@Composable
private fun Chevron() {
    Icon(
        Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyValue() {
    Text(
        text = "—",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SmallPrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = RhSpacing.lg, vertical = RhSpacing.xs),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SmallOutlinedButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = RhSpacing.lg, vertical = RhSpacing.xs),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ---- helpers ----------------------------------------------------------------------------------

/** "muhammad…fik92@gmail.com" → "muham******fik92@gmail.com". Best-effort masking for display. */
private fun maskEmail(email: String): String {
    val at = email.indexOf('@')
    if (at <= 0) return email
    val local = email.substring(0, at)
    val domain = email.substring(at)
    if (local.length <= 6) return email
    return local.take(5) + "******" + local.takeLast(2) + domain
}
