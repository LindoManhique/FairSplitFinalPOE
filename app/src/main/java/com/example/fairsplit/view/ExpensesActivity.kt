package com.example.fairsplit.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fairsplit.R
import com.example.fairsplit.controller.ExpensesController
import com.example.fairsplit.databinding.ActivityExpensesBinding
import com.example.fairsplit.model.dto.Expense
import com.example.fairsplit.util.Nav
import com.example.fairsplit.util.NavStore
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ExpensesActivity : AppCompatActivity() {

    private lateinit var b: ActivityExpensesBinding

    private lateinit var groupId: String
    private lateinit var groupName: String

    private val items = mutableListOf<Expense>()
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var ctrl: ExpensesController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ---- Read navigation args ----
        groupId = intent.getStringExtra("groupId") ?: ""
        groupName = intent.getStringExtra("groupName") ?: getString(R.string.title_expenses)
        b.tvScreenTitle.text = "${getString(R.string.title_expenses)} — $groupName"

        // ---- Bottom nav (select Expenses tab) ----
        NavStore.saveLastGroup(this, groupId, groupName)
        Nav.setup(
            activity = this,
            bottomNav = b.bottomNav,
            selectedTabId = R.id.tab_expenses,
            groupId = groupId,
            groupName = groupName
        )

        // ---- List adapter ----
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        b.listExpenses.adapter = adapter

        // ---- Controller (live updates) ----
        ctrl = ExpensesController(ui = { action ->
            when (action) {
                is ExpensesController.Action.Loading -> setLoading(action.on)

                is ExpensesController.Action.Error ->
                    toast(action.msg)

                is ExpensesController.Action.Items -> {
                    items.clear()
                    items.addAll(action.items)
                    refreshAdapter()
                }

                is ExpensesController.Action.Added -> {
                    items.add(0, action.item)
                    refreshAdapter()
                }

                is ExpensesController.Action.Removed -> {
                    val idx = items.indexOfFirst { it.id == action.id }
                    if (idx >= 0) {
                        items.removeAt(idx)
                        refreshAdapter()
                    }
                }
            }
        })

        // ---- Add expense ----
        b.btnAdd.setOnClickListener {
            val title = b.etTitle.text?.toString()?.trim().orEmpty()
            val amountText = b.etAmount.text?.toString()?.trim().orEmpty()
            val amountParsed = amountText.toDoubleOrNull()

            var invalid = false
            if (title.isEmpty()) { b.etTitle.error = getString(R.string.error_title_required); invalid = true }
            if (amountParsed == null || amountParsed <= 0.0) { b.etAmount.error = getString(R.string.error_amount_required); invalid = true }
            if (invalid) return@setOnClickListener

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val amt = amountParsed!!   // safe due to validation

            val e = Expense(
                id = "",                           // repo will assign real id
                groupId = groupId,
                title = title,
                amount = amt,                      // non-null Double
                payerUid = uid,
                participants = listOf(uid)
            )

            // Controller handles optimistic UI + background save
            ctrl.add(groupId, e)

            // Clear inputs
            b.etTitle.setText("")
            b.etAmount.setText("")
        }

        // ---- Long-press delete ----
        b.listExpenses.setOnItemLongClickListener { _, _, position, _ ->
            val toDelete = items.getOrNull(position) ?: return@setOnItemLongClickListener true
            val id = toDelete.id
            if (id.isNullOrBlank()) {
                toast(getString(R.string.msg_syncing_please_wait))
                return@setOnItemLongClickListener true
            }
            ctrl.delete(groupId, id)
            true
        }
    }

    // Start/stop live listener so screen stays in sync
    override fun onStart() {
        super.onStart()
        ctrl.watch(groupId)
    }

    override fun onStop() {
        super.onStop()
        ctrl.unwatch()
    }

    override fun onDestroy() {
        super.onDestroy()
        ctrl.clear()
    }

    // ---------- Helpers ----------
    private fun refreshAdapter() {
        val rows = items.map { exp ->
            val amt = String.format(Locale.getDefault(), "R %.2f", exp.amount)
            "${exp.title} — $amt"
        }
        adapter.clear()
        adapter.addAll(rows)
        adapter.notifyDataSetChanged()
    }

    private fun setLoading(on: Boolean) {
        b.progress.visibility = if (on) View.VISIBLE else View.GONE
        b.btnAdd.isEnabled = !on
        b.etTitle.isEnabled = !on
        b.etAmount.isEnabled = !on
        b.listExpenses.isEnabled = !on
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
