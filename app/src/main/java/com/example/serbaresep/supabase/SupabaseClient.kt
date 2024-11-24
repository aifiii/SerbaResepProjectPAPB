package com.example.supabaseapp

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.serializer


val supabase = createSupabaseClient(
    supabaseUrl = "https://exjwylkeissymjyoikya.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV4and5bGtlaXNzeW1qeW9pa3lhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzIyNTUwNTYsImV4cCI6MjA0NzgzMTA1Nn0.Pb6Go42aTKkjvTC_aRxu9bPt7XCvolT6U7wxer5pTNA"
) {

    install(Auth) // Untuk autentikasi
    install(Postgrest) // Untuk operasi database
    install(Storage)
}
