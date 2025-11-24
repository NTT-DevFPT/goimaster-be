package com.goimaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;
    
    @Value("${supabase.service-key}")
    private String supabaseServiceKey;
    
    public String getSupabaseUrl() {
        return supabaseUrl;
    }
    
    public String getSupabaseAnonKey() {
        return supabaseAnonKey;
    }
    
    public String getSupabaseServiceKey() {
        return supabaseServiceKey;
    }
}


