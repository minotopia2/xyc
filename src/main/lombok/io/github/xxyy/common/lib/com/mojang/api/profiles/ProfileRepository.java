package io.github.xxyy.common.lib.com.mojang.api.profiles;

public interface ProfileRepository {
    public Profile[] findProfilesByCriteria(ProfileCriteria... criteria);
}