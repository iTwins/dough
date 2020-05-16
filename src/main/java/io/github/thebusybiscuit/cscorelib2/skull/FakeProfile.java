package io.github.thebusybiscuit.cscorelib2.skull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.inventory.meta.SkullMeta;

import io.github.thebusybiscuit.cscorelib2.reflection.ReflectionUtils;
import lombok.NonNull;

public final class FakeProfile {

    protected static final String PROPERTY_KEY = "textures";

    private FakeProfile() {}

    protected static Method property;
    protected static Method insertProperty;

    protected static Constructor<?> profileConstructor;
    protected static Constructor<?> propertyConstructor;

    protected static Class<?> profileClass;
    protected static Class<?> propertyClass;
    protected static Class<?> mapClass;

    static {
        try {
            profileClass = Class.forName("com.mojang.authlib.GameProfile");
            propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            mapClass = Class.forName("com.mojang.authlib.properties.PropertyMap");

            profileConstructor = ReflectionUtils.getConstructor(profileClass, UUID.class, String.class);
            property = ReflectionUtils.getMethod(profileClass, "getProperties");
            propertyConstructor = ReflectionUtils.getConstructor(propertyClass, String.class, String.class);
            insertProperty = ReflectionUtils.getMethod(mapClass, "put", String.class, propertyClass);
        }
        catch (Exception e) {
            System.err.println("Perhaps you forgot to shade CS-CoreLib's \"reflection\" package?");
            e.printStackTrace();
        }
    }

    protected static Object createProfile(@NonNull UUID uuid, @NonNull String texture) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object profile = profileConstructor.newInstance(uuid, "CS-CoreLib");
        Object properties = property.invoke(profile);
        insertProperty.invoke(properties, PROPERTY_KEY, propertyConstructor.newInstance(PROPERTY_KEY, texture));
        return profile;
    }

    protected static void inject(@NonNull SkullMeta meta, @NonNull UUID uuid, @NonNull String texture) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        ReflectionUtils.setFieldValue(meta, "profile", createProfile(uuid, texture));
        
        // Forces SkullMeta to properly deserialize and serialize the profile
        meta.setOwningPlayer(meta.getOwningPlayer());
        
        // Now override the texture again
        ReflectionUtils.setFieldValue(meta, "profile", createProfile(uuid, texture));
    }

}
