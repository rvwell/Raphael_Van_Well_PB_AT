package com.infnet.pb.AT.model;

/**
 * Enum que define os tipos de roles disponíveis no sistema.
 * Substitui a entidade Role por uma estrutura mais rígida e type-safe.
 */
public enum RoleType {
    USER("USER", "Usuário padrão do sistema"),
    ADMIN("ADMIN", "Administrador com acesso completo");

    private final String name;
    private final String description;

    RoleType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Retorna o nome do role com prefixo ROLE_ para compatibilidade com Spring Security
     */
    public String getAuthority() {
        return "ROLE_" + name;
    }

    /**
     * Converte uma string para RoleType
     */
    public static RoleType fromString(String name) {
        for (RoleType role : RoleType.values()) {
            if (role.name.equalsIgnoreCase(name) || 
                role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Role não encontrado: " + name);
    }
}