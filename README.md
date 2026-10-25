# Ovyu — MVP acadêmico (Android)

App Android 100% offline: tudo é salvo no aparelho com **Room**, e a **sessão** do usuário
fica no **DataStore** (guarda o `id` do paciente logado, usado ao salvar qualquer registro).

**Stack:** Kotlin · Jetpack Compose (Material 3) · MVVM · Room · DataStore · Navigation Compose

## Como abrir
1. Android Studio → *File ▸ Open* → selecione a pasta `Ovyu`.
2. Aguarde o *Gradle Sync* (o Android Studio cria o `local.properties` sozinho).
3. Rode em um emulador/aparelho com **Android 8.0 (API 26) ou superior**.

## Fluxo do app
- **Abertura:** lê o DataStore → com sessão vai para a Home; sem sessão vai para o Login.
- **Login / Cadastro:** senha com PBKDF2 (salt + hash) no Room; ao entrar grava o `id` no DataStore.
- **Home:** acesso rápido (Sintomas, Medicamentos, Consultas, Exames), próximas consultas reais,
  notificações (avisos gerados dos dados locais) e atalho para "Minha saúde".
- **Abas:** Início · Saúde (resumo + gráfico de intensidade) · Histórico (filtro por tipo) · Perfil.
- **CRUD completo** de Sintomas, Medicamentos, Consultas e Exames (criar, listar, editar, excluir).
- **Perfil:** editar nome, sair da conta (sessão limpa, dados mantidos) e excluir conta (apaga tudo em cascata).

## Estrutura (pacote `com.example.ovyu`)
```
room/        entidades, DAOs, OvyuDatabase, SessionManager (DataStore)
repository/  AuthRepository + um repositório por entidade
viewmodel/   ViewModels, estados de UI e AppViewModelProvider (factory)
compose/     telas (login, singup, home, health, history, profile, register,
             medication, appointment, exam, notification), components/, navegação
ui/theme/    cores da marca, tema e tipografia
util/        formatação e conversão de datas
AppContainer.kt  injeção de dependência manual + OvyuApplication
```

## Observações
- O banco está na versão 2 com `fallbackToDestructiveMigration`: se já instalou uma versão anterior,
  desinstale o app (ou limpe os dados) antes de rodar.
- Login social e "Esqueceu sua senha?" mostram aviso "em breve" (exigem serviços externos).
- O KSP (`ksp` em `gradle/libs.versions.toml`) é o que faz o Room gerar o código dos DAOs.
