/*
 * MekWars - Copyright (C) 2026
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package mekwars.common.campaign.persistence.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import megamek.Version;

@Converter(autoApply = true)
public class VersionConverter implements AttributeConverter<Version, String> {
    @Override
    public String convertToDatabaseColumn(Version attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public Version convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Version(dbData);
    }
}
