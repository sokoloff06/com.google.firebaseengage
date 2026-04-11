package com.google.firebaseengage.ui.cart

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebaseengage.data.entities.Cart
import com.google.firebaseengage.ui.MainActivity
import androidx.compose.ui.graphics.Color as ComposeColor

class CartFragment : Fragment() {

    companion object {
        const val KEY_PURCHASE_BTN_COLOR = "btn_buy_color"
    }

    private lateinit var cartHandler: CartHandler
    private lateinit var cart: Cart

    // State for compose
    private var btnColor = mutableStateOf(ComposeColor.White)
    private var cartSum = mutableStateOf(0)
    private var cartItems = mutableStateOf<List<Cart.ProductRecord>>(emptyList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cartHandler = context as? CartHandler
            ?: throw ClassCastException("$context must implement CartHandler interface")
        cart = cartHandler.cart
        updateState()
    }

    override fun onResume() {
        super.onResume()
        if (::cart.isInitialized) {
            updateState()
        }
    }

    private fun updateState() {
        val colorStr = FirebaseRemoteConfig.getInstance().getString(KEY_PURCHASE_BTN_COLOR)
        val parsedColor = try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            Color.WHITE
        }
        btnColor.value = ComposeColor(parsedColor)
        cartSum.value = cart.sum
        cartItems.value = cart.products.values.toList()
    }

    fun onSwipeUpdate() {
        if (::cart.isInitialized) {
            updateState()
            Log.d(MainActivity.LOG_TAG, "Applied btn_buy_color from Remote Config")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    CartScreen(
                        items = cartItems.value,
                        sum = cartSum.value,
                        btnColor = btnColor.value,
                        onAdd = { pr ->
                            cart.add(pr)
                            updateState()
                            cartHandler.onDataHasChanged()
                        },
                        onRemove = { pr ->
                            cart.remove(pr)
                            updateState()
                            cartHandler.onDataHasChanged()
                        },
                        onCountChange = { pr, newCount ->
                            pr.count = newCount
                            cart.set(pr)
                            updateState()
                            cartHandler.onDataHasChanged()
                        },
                        onPurchase = { transactionId ->
                            performPurchase()
                        }
                    )
                }
            }
        }
    }

    private fun performPurchase() {
        // TODO: diplay toast
    }
}

@Composable
fun CartScreen(
    items: List<Cart.ProductRecord>,
    sum: Int,
    btnColor: ComposeColor,
    onAdd: (Cart.ProductRecord) -> Unit,
    onRemove: (Cart.ProductRecord) -> Unit,
    onCountChange: (Cart.ProductRecord, Int) -> Unit,
    onPurchase: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(items) { item ->
                CartItemRow(
                    item = item,
                    onAdd = { onAdd(item) },
                    onRemove = { onRemove(item) },
                    onCountChange = { count -> onCountChange(item, count) }
                )
            }
        }

        Surface(
            elevation = 16.dp,
            modifier = Modifier.fillMaxWidth(),
            color = ComposeColor.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ORDER COST",
                        fontSize = 18.sp,
                        color = ComposeColor.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "${sum}€",
                        fontSize = 18.sp,
                        color = ComposeColor.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                var transactionId by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Transaction ID (Optional)") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onPurchase(transactionId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = btnColor)
                ) {
                    Text("BUY", color = ComposeColor.Black)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: Cart.ProductRecord,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = Uri.parse(item.pic),
            contentDescription = item.name,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = item.name ?: "",
            modifier = Modifier.weight(1f),
            color = ComposeColor.Black,
            fontSize = 16.sp
        )
        
        Button(
            onClick = onRemove,
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("-")
        }
        
        var countText by remember(item.count) { mutableStateOf(item.count.toString()) }
        
        OutlinedTextField(
            value = countText,
            onValueChange = { 
                countText = it
                it.toIntOrNull()?.let { count -> onCountChange(count) }
            },
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Button(
            onClick = onAdd,
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+")
        }
    }
}
