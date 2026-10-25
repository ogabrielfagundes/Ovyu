package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ovyu.AppContainer
import com.example.ovyu.OvyuApplication

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as OvyuApplication).container

/**
 * Factory única de ViewModels: entrega as dependências do AppContainer
 * (repositórios e sessão) para cada ViewModel.
 *
 * Uso nas telas: viewModel(factory = AppViewModelProvider.Factory)
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = container()
            SessionViewModel(c.sessionManager, c.authRepository)
        }
        initializer {
            val c = container()
            AuthViewModel(c.authRepository, c.sessionManager)
        }
        initializer {
            val c = container()
            HomeViewModel(
                c.sessionManager,
                c.authRepository,
                c.appointmentRepository,
                c.medicationRepository,
                c.examRepository
            )
        }
        initializer {
            val c = container()
            HealthViewModel(
                c.sessionManager,
                c.symptomRepository,
                c.medicationRepository,
                c.appointmentRepository,
                c.examRepository
            )
        }
        initializer {
            val c = container()
            HistoryViewModel(
                c.sessionManager,
                c.symptomRepository,
                c.medicationRepository,
                c.appointmentRepository,
                c.examRepository
            )
        }
        initializer {
            val c = container()
            ProfileViewModel(
                c.sessionManager,
                c.authRepository,
                c.symptomRepository,
                c.medicationRepository,
                c.appointmentRepository,
                c.examRepository
            )
        }
        initializer {
            val c = container()
            NotificationsViewModel(
                c.sessionManager,
                c.appointmentRepository,
                c.medicationRepository,
                c.examRepository
            )
        }
        initializer {
            val c = container()
            SymptomFormViewModel(createSavedStateHandle(), c.sessionManager, c.symptomRepository)
        }
        initializer {
            val c = container()
            MedicationsViewModel(c.sessionManager, c.medicationRepository)
        }
        initializer {
            val c = container()
            MedicationFormViewModel(createSavedStateHandle(), c.sessionManager, c.medicationRepository)
        }
        initializer {
            val c = container()
            AppointmentsViewModel(c.sessionManager, c.appointmentRepository)
        }
        initializer {
            val c = container()
            AppointmentFormViewModel(createSavedStateHandle(), c.sessionManager, c.appointmentRepository)
        }
        initializer {
            val c = container()
            ExamsViewModel(c.sessionManager, c.examRepository)
        }
        initializer {
            val c = container()
            ExamFormViewModel(createSavedStateHandle(), c.sessionManager, c.examRepository)
        }
    }
}
