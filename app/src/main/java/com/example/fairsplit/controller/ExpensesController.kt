package com.example.fairsplit.controller

import com.example.fairsplit.model.dto.Expense
import com.example.fairsplit.model.remote.FirestoreRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*

class ExpensesController(
    private val ui: (Action) -> Unit,
    private val repo: FirestoreRepository = FirestoreRepository()
) {
    // Messages sent back to the Activity/Fragment
    sealed class Action {
        data class Loading(val on: Boolean) : Action()
        data class Error(val msg: String) : Action()
        data class Items(val items: List<Expense>) : Action()
        data class Added(val item: Expense) : Action()
        data class Removed(val id: String) : Action()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listener: ListenerRegistration? = null

    companion object { private const val TIMEOUT_MS = 20_000L }

    // ---------------- One-shot load (manual refresh) ----------------
    fun load(groupId: String) {
        scope.launch {
            ui(Action.Loading(true))
            try {
                val items = withTimeout(TIMEOUT_MS) {
                    withContext(Dispatchers.IO) { repo.listExpenses(groupId) }
                }
                ui(Action.Items(items))
            } catch (_: TimeoutCancellationException) {
                ui(Action.Error("Loading expenses timed out."))
            } catch (e: Exception) {
                ui(Action.Error(e.message ?: "Failed to load expenses"))
            } finally {
                ui(Action.Loading(false))
            }
        }
    }

    // ---------------- Live updates ----------------
    /** Start listening for changes; call unwatch() in onStop/onDestroy. */
    fun watch(groupId: String) {
        unwatch()
        ui(Action.Loading(true))
        listener = repo.listenExpenses(
            groupId = groupId,
            onChange = { items ->
                ui(Action.Items(items))
                ui(Action.Loading(false))
            },
            onError = { msg ->
                ui(Action.Error(msg))
                ui(Action.Loading(false))
            }
        )
        // optional initial fetch
        load(groupId)
    }

    /** Stop listening to Firestore changes. */
    fun unwatch() {
        listener?.remove()
        listener = null
    }

    // ---------------- Add (optimistic) ----------------
    fun add(groupId: String, expense: Expense) {
        scope.launch {
            // Optimistic UI
            ui(Action.Loading(true))
            ui(Action.Added(expense))
            ui(Action.Loading(false)) // stop spinner immediately

            // Background save + best-effort refresh
            launch(Dispatchers.IO) {
                withTimeoutOrNull(TIMEOUT_MS) { repo.addExpense(groupId, expense) }
                val fresh = runCatching { repo.listExpenses(groupId) }.getOrElse { emptyList() }
                withContext(Dispatchers.Main) { if (fresh.isNotEmpty()) ui(Action.Items(fresh)) }
            }
        }
    }

    // ---------------- Delete ----------------
    fun delete(groupId: String, expenseId: String) {
        scope.launch {
            ui(Action.Loading(true))
            try {
                withContext(Dispatchers.IO) {
                    withTimeoutOrNull(TIMEOUT_MS) { repo.deleteExpense(groupId, expenseId) }
                }
                ui(Action.Removed(expenseId))

                val fresh = withContext(Dispatchers.IO) {
                    runCatching { repo.listExpenses(groupId) }.getOrElse { emptyList() }
                }
                ui(Action.Items(fresh))
            } catch (e: Exception) {
                ui(Action.Error(e.message ?: "Could not delete expense"))
            } finally {
                ui(Action.Loading(false))
            }
        }
    }

    // ---------------- Lifecycle cleanup ----------------
    fun clear() {
        unwatch()
        scope.cancel()
    }
}
