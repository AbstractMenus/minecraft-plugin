package ru.abstractmenus.api.variables;

/**
 * Manager for variables. Here you can use CRUD methods to manipulate with variables
 */
public interface VariableManager {

    /**
     * Get global variable
     * @param name Key of variable
     * @return Found variable or null
     */
    Var getGlobal(String name);

    /**
     * Get personal variable
     * @param username Owner if variable
     * @param name Key of variable
     * @return Found variable of null
     */
    Var getPersonal(String username, String name);

    /**
     * Save variable as global
     * @param var Variable data
     * @param replace If false, then stored variable won't be replaced
     */
    void saveGlobal(Var var, boolean replace);

    /**
     * Create or update variable as personal for some player
     * @param var Variable data
     */
    default void saveGlobal(Var var) {
        saveGlobal(var, true);
    }

    /**
     * Save variable as personal for some player
     * @param var Variable data
     * @param username Variable owner
     * @param replace If false, then stored variable won't be replaced
     */
    void savePersonal(String username, Var var, boolean replace);

    /**
     * Create or update variable as personal for some player
     * @param var Variable data
     * @param username Variable owner
     */
    default void savePersonal(String username, Var var) {
        savePersonal(username, var, true);
    }

    /**
     * Delete global variable by name
     * @param name Name of variable
     */
    void deleteGlobal(String name);

    /**
     * Delete personal variable by name and owner
     * @param username Variable owner
     * @param name Name of variable
     */
    void deletePersonal(String username, String name);

    /**
     * Create builder for variable
     * @return New variable builder
     */
    VarBuilder createBuilder();

}
