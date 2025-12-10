package com.streaks;

import com.streaks.StreaksPlugin.SkillType;

public interface StreakContext
{
    void handleSkillSuccess(SkillType skill, String target);
    void handleSkillFailure(SkillType skill);
    void handleSkillFailure(SkillType skill, String target);
}