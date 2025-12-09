package com.streaks;

import com.streaks.StreaksPlugin.SkillType;

public interface StreakContext
{
    void handleSkillSuccess(SkillType skill, int targetId);
    void handleSkillSuccess(SkillType skill, String target);
    void handleSkillFailure(SkillType skill, int targetId);
    void handleSkillFailure(SkillType skill, String target);
}