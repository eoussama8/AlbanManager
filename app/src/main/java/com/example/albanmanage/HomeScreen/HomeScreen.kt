package com.example.albanmanage.HomeScreen

import com.example.albanmanage.R
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.albanmanage.Models.Product
import com.example.albanmanage.Models.ProductRepository
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey
import com.example.albanmanage.ui.theme.AlbaneRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.text.selection.TextSelectionColors
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.res.stringResource
import com.example.albanmanage.HistoryScreen.HistoryDao
import com.example.albanmanage.HistoryScreen.HistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

data class ProductData(
    val name: String,
    val unitPrice: Double,
    val packSize: Int,
    val units: Int,
    val packs: Int,
    val expired: Int,
    val disabled: Int
)



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(currentLanguage: String, historyDao: HistoryDao)
 {

    // Apply language locale dynamically

    val context = LocalContext.current
    val productDataMap = remember { mutableStateMapOf<String, ProductData>() }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    LaunchedEffect(currentLanguage) {
        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    // Calculate totals
    fun calculateGrandTotals(productMap: Map<String, ProductData>): Pair<Double, Double> {
        return productMap.values.fold(0.0 to 0.0) { (totalBefore, totalAfter), product ->
            val totalUnits = product.units + product.packs * product.packSize
            val beforeDeduction = (totalUnits + product.expired + product.disabled) * product.unitPrice
            val afterDeduction = ((totalUnits - product.expired - product.disabled).coerceAtLeast(0) * product.unitPrice)
            (totalBefore + beforeDeduction) to (totalAfter + afterDeduction)
        }
    }

    val (grandTotalBefore, grandTotalAfter) = calculateGrandTotals(productDataMap)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Header
        item {
            EnhancedHeader()
        }

        // Summary Cards
        item {
            SummarySection(grandTotalBefore, grandTotalAfter)
        }

        // Product forms
        items(ProductRepository.getProducts(context)) { product ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically() + fadeIn(),
                modifier = Modifier.animateItemPlacement()
            ) {
                EnhancedProductForm(product) { data ->
                    productDataMap[product.name] = data
                }
            }
        }

        // Generate PDF Button
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (productDataMap.isNotEmpty()) {
                        showInvoiceDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.no_products_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlbaneBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isGeneratingPdf
            ) {
                if (isGeneratingPdf) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.generating_pdf), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_pdf),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.generate_pdf), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Invoice Dialog
     if (showInvoiceDialog) {
         AlertDialog(
             onDismissRequest = { showInvoiceDialog = false },
             title = { Text(stringResource(R.string.generate_invoice)) },
             text = { Text(stringResource(R.string.confirm_generate_invoice)) },
             confirmButton = {
                 TextButton(
                     onClick = {
                         showInvoiceDialog = false
                         isGeneratingPdf = true
                         CoroutineScope(Dispatchers.IO).launch {
                             val pdfBytes = generatePdfBytes(productDataMap.values.toList(), context)

                             val title = context.getString(
                                 R.string.pdf_invoice_title,
                                 java.text.SimpleDateFormat(
                                     "yyyy-MM-dd",
                                     java.util.Locale.getDefault()
                                 ).format(java.util.Date())
                             )

                             // In your AlertDialog confirmButton onClick:
                             withContext(Dispatchers.Main) {
                                 savePdfToDownloads(
                                     context,
                                     title,
                                     pdfBytes
                                 ) { savedFileName ->
                                     if (savedFileName.isNotEmpty()) {
                                         // Calculate totals correctly
                                         val (totalBefore, totalAfter) = calculateGrandTotals(productDataMap)

                                         CoroutineScope(Dispatchers.IO).launch {
                                             val historyItem = HistoryEntity(
                                                 actionType = "PDF Generated",
                                                 fileName = savedFileName, // Use the actual saved filename with timestamp
                                                 date = System.currentTimeMillis(),
                                                 totalBefore = totalBefore,
                                                 totalAfter = totalAfter,
                                                 productCount = productDataMap.size
                                             )
                                             historyDao.insert(historyItem)
                                         }
                                     }
                                     isGeneratingPdf = false
                                 }
                             }
                         }
                     }
                 ) {
                     Text(
                         stringResource(R.string.confirm_generate),
                         color = AlbaneBlue,
                         fontWeight = FontWeight.Bold
                     )
                 }
             },
             dismissButton = {
                 TextButton(onClick = { showInvoiceDialog = false }) {
                     Text(stringResource(R.string.cancel))
                 }
             }
         )
     }}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnhancedHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        // Wrap content with Box if you want alignment inside the Card
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                )

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlbaneBlue
                )

                Text(
                    text = stringResource(R.string.app_tagline),
                    fontSize = 14.sp,
                    color = AlbaneGrey,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            AlbaneBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AlbaneBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LocalDate.now().format(
                            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                        ),
                        fontSize = 14.sp,
                        color = AlbaneBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SummarySection(totalBefore: Double, totalAfter: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = stringResource(R.string.total_before),
            amount = totalBefore,
            color = AlbaneBlue,
            iconRes = R.drawable.ic_trending_up,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = stringResource(R.string.total_after),
            amount = totalAfter,
            color = AlbaneRed,
            iconRes = R.drawable.ic_money,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = AlbaneGrey,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = stringResource(R.string.total_before_amount, amount)
                        .replace("Total Before", ""),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductForm(product: Product, onDataChange: (ProductData) -> Unit) {
    var units by remember { mutableStateOf("") }
    var packs by remember { mutableStateOf("") }
    var expired by remember { mutableStateOf("") }
    var disabled by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    val unitsCount = units.toIntOrNull() ?: 0
    val packsCount = packs.toIntOrNull() ?: 0
    val expiredCount = expired.toIntOrNull() ?: 0
    val disabledCount = disabled.toIntOrNull() ?: 0

    val totalUnits = unitsCount + packsCount * product.pack
    val totalBeforeUnits = totalUnits + expiredCount + disabledCount
    val totalAfterUnits = (totalUnits - expiredCount - disabledCount).coerceAtLeast(0)

    val totalBeforePrice = totalBeforeUnits * product.price
    val totalAfterPrice = totalAfterUnits * product.price

    // Update parent with current data
    LaunchedEffect(unitsCount, packsCount, expiredCount, disabledCount) {
        onDataChange(
            ProductData(
                name = product.name,
                unitPrice = product.price,
                packSize = product.pack,
                units = unitsCount,
                packs = packsCount,
                expired = expiredCount,
                disabled = disabledCount
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Product Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AlbaneBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(product.imageResId),
                        contentDescription = product.name,
                        modifier = Modifier.size(60.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = stringResource(R.string.unit_price_pack, product.price, product.pack),
                        fontSize = 14.sp,
                        color = AlbaneGrey
                    )
                }

                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AlbaneBlue
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Fields
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EnhancedTextField(
                            value = units,
                            onValueChange = { units = it.filter { c -> c.isDigit() } },
                            label = stringResource(R.string.units),
                            iconRes = R.drawable.ic_units,
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedTextField(
                            value = packs,
                            onValueChange = { packs = it.filter { c -> c.isDigit() } },
                            label = stringResource(R.string.packs),
                            iconRes = R.drawable.ic_package,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EnhancedTextField(
                            value = expired,
                            onValueChange = { expired = it.filter { c -> c.isDigit() } },
                            label = stringResource(R.string.expired),
                            iconRes = R.drawable.ic_expired,
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedTextField(
                            value = disabled,
                            onValueChange = { disabled = it.filter { c -> c.isDigit() } },
                            label = stringResource(R.string.disabled),
                            iconRes = R.drawable.ic_disabled,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculation Results
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CalculationRow(stringResource(R.string.total_units), "$totalUnits units", AlbaneGrey)
                            Spacer(modifier = Modifier.height(8.dp))
                            CalculationRow(stringResource(R.string.before_deduction), stringResource(R.string.total_before_amount, totalBeforePrice).replace("Total Before", ""), AlbaneBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            CalculationRow(stringResource(R.string.after_deduction), stringResource(R.string.total_after_amount, totalAfterPrice).replace("Total After", ""), AlbaneRed)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Gray,
            errorTextColor = Color.Red,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.LightGray,
            errorContainerColor = Color.White,
            cursorColor = AlbaneBlue,
            errorCursorColor = Color.Red,
            selectionColors = TextSelectionColors(
                handleColor = AlbaneBlue,
                backgroundColor = AlbaneBlue.copy(alpha = 0.4f)
            ),
            focusedBorderColor = AlbaneBlue,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red,
            focusedLeadingIconColor = AlbaneBlue,
            unfocusedLeadingIconColor = AlbaneGrey,
            focusedTrailingIconColor = AlbaneBlue,
            unfocusedTrailingIconColor = AlbaneGrey,
            focusedLabelColor = AlbaneBlue,
            unfocusedLabelColor = AlbaneGrey
        )
        ,
        singleLine = true
    )

}

@Composable
fun CalculationRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// PDF Functions
fun savePdfToDownloads(
    context: Context,
    fileName: String,
    pdfBytes: ByteArray,
    onComplete: (String) -> Unit
) {
    var savedFileName = ""
    try {
        savedFileName = "$fileName ${System.currentTimeMillis()}.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, savedFileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it).use { output ->
                    output?.write(pdfBytes)
                    output?.flush()
                }
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            } ?: throw IOException("Failed to create file URI")
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                throw IOException("Failed to create Downloads directory")
            }
            val file = File(downloadsDir, savedFileName)
            FileOutputStream(file).use { it.write(pdfBytes) }
        }
        Toast.makeText(context, context.getString(R.string.pdf_saved, savedFileName), Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
        Log.e("PDF_ERROR", "Permission error", e)
        Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        savedFileName = "" // Failed to save
    } catch (e: IOException) {
        Log.e("PDF_ERROR", "IO error", e)
        Toast.makeText(context, context.getString(R.string.pdf_save_failed, e.message ?: "IO error"), Toast.LENGTH_LONG).show()
        savedFileName = "" // Failed to save
    } catch (e: Exception) {
        Log.e("PDF_ERROR", "Unexpected error", e)
        Toast.makeText(context, context.getString(R.string.unexpected_error, e.message ?: "Unknown error"), Toast.LENGTH_LONG).show()
        savedFileName = "" // Failed to save
    } finally {
        // Return the actual filename or empty string if failed
        onComplete(savedFileName)
    }
}

fun generatePdfBytes(productList: List<ProductData>, context: Context): ByteArray {
    val document = PdfDocument()
    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
    if (page == null) {
        Log.e("PDF_ERROR", "Failed to create PDF page")
        document.close()
        return ByteArray(0)
    }
    val canvas = page.canvas
    val paint = android.graphics.Paint()
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 12f
    paint.isAntiAlias = true // Improve text rendering quality

    var y = 160f

    // Title
    paint.textSize = 24f
    paint.isFakeBoldText = true
    try {
        canvas.drawText("AlbanManage Invoice", 50f, 80f, paint)
    } catch (e: Exception) {
        Log.e("PDF_ERROR", "Failed to draw title", e)
        document.finishPage(page)
        document.close()
        return ByteArray(0)
    }

    // Date
    paint.textSize = 14f
    paint.isFakeBoldText = false
    val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    canvas.drawText("Date: $currentDate", 50f, 110f, paint)

    // Headers
    paint.textSize = 12f
    paint.isFakeBoldText = true
    // Draw header background
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawRect(45f, y - 20f, 550f, y + 5f, paint)
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("Product", 50f, y, paint)
    canvas.drawText("Units", 180f, y, paint)
    canvas.drawText("Packs", 230f, y, paint)
    canvas.drawText("Quantity", 280f, y, paint)
    canvas.drawText("Expired", 340f, y, paint)
    canvas.drawText("Disabled", 390f, y, paint)
    canvas.drawText("Price", 440f, y, paint)
    canvas.drawText("Total", 500f, y, paint)
    y += 25f
    paint.isFakeBoldText = false

    var grandTotal = 0.0

    productList.forEach { product ->
        // Check for page overflow
        if (y > 750f) {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            if (page == null) {
                Log.e("PDF_ERROR", "Failed to create new PDF page")
                document.close()
                return ByteArray(0)
            }
            y = 50f
            // Redraw headers
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.LTGRAY
            canvas.drawRect(45f, y - 20f, 550f, y + 5f, paint)
            paint.color = android.graphics.Color.BLACK
            canvas.drawText("Product", 50f, y, paint)
            canvas.drawText("Units", 180f, y, paint)
            canvas.drawText("Packs", 230f, y, paint)
            canvas.drawText("Total Units", 280f, y, paint)
            canvas.drawText("Expired", 340f, y, paint)
            canvas.drawText("Disabled", 390f, y, paint)
            canvas.drawText("Unit Price", 440f, y, paint)
            canvas.drawText("Total", 500f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
        }

        val totalUnits = product.units + product.packs * product.packSize
        val finalUnits = (totalUnits - product.expired - product.disabled).coerceAtLeast(0)
        val totalPrice = finalUnits * product.unitPrice
        grandTotal += totalPrice

        // Truncate long product names
        val maxWidth = 110f
        val truncatedName = if (paint.measureText(product.name) > maxWidth) {
            product.name.takeWhile { paint.measureText(it.toString()) < maxWidth - paint.measureText("...") } + "..."
        } else {
            product.name
        }

        canvas.drawText(truncatedName, 50f, y, paint)
        canvas.drawText(product.units.toString(), 180f, y, paint)
        canvas.drawText(product.packs.toString(), 230f, y, paint)
        canvas.drawText(totalUnits.toString(), 280f, y, paint)
        canvas.drawText(product.expired.toString(), 340f, y, paint)
        canvas.drawText(product.disabled.toString(), 390f, y, paint)
        canvas.drawText(String.format("%.2f", product.unitPrice), 440f, y, paint)
        canvas.drawText("${String.format("%.2f", totalPrice)} MAD", 500f, y, paint)
        y += 20f
    }

    // Grand Total
    if (y > 750f) {
        document.finishPage(page)
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        if (page == null) {
            Log.e("PDF_ERROR", "Failed to create new PDF page for total")
            document.close()
            return ByteArray(0)
        }
        y = 50f
    }
    y += 20f
    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("Grand Total: ${String.format("%.2f", grandTotal)} MAD", 50f, y, paint)

    // Footer
    y += 40f
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText("Generated by AlbanManage", 50f, y, paint)

    document.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    try {
        document.writeTo(outputStream)
    } catch (e: Exception) {
        Log.e("PDF_ERROR", "Failed to write PDF to output stream", e)
        return ByteArray(0)
    } finally {
        document.close()
    }
    return outputStream.toByteArray()
}