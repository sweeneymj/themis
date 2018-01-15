/*******************************************************************
 Themis - NetFlow scoring and tagging framework.
 Version: 0.5
 Release date: 2017/12/31
 Author: MJ Sweeney
 Rhodes University
 Computer Science Masters Project - 2017
 Supervisor: Barry Irwin
 Copyright (C) 2017, MJ Sweeney
 *******************************************************************/
package com.verifydynamics.netflowscoring.domain;

/****************************************************************************
 * stores the score and the score code from a test
 ****************************************************************************/

public class FlowScore {
    private static final long serialVersionUID = 1L;

    String  score_category;
    String  score_code;
    int score;

    public String getScore_category() {
        return score_category;
    }

    public void setScore_category(String score_category) {
        this.score_category = score_category;
    }

    public String getScore_code() {
        return score_code;
    }

    public void setScore_code(String score_code) {
        this.score_code = score_code;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowScore)) return false;

        FlowScore flowScore = (FlowScore) o;

        if (score != flowScore.score) return false;
        if (score_category != null ? !score_category.equals(flowScore.score_category) : flowScore.score_category != null)
            return false;
        if (score_code != null ? !score_code.equals(flowScore.score_code) : flowScore.score_code != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = score_category != null ? score_category.hashCode() : 0;
        result = 31 * result + (score_code != null ? score_code.hashCode() : 0);
        result = 31 * result + score;
        return result;
    }
}
