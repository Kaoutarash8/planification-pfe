package com.ensa.dao.interfaces;

import com.ensa.model.ConfigurationSoutenance;

public interface ConfigurationDao {
    ConfigurationSoutenance lireConfiguration(String filePath) throws Exception;
}