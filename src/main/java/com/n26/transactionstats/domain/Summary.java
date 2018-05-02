package com.n26.transactionstats.domain;

public class Summary {
    private Double sum;
    private Double avg;
    private Double max;
    private Double min;
    private Long count;

    public Summary() {
        sum = 0.0;
        avg = 0.0;
        max = 0.0;
        min = Double.MAX_VALUE;
        count = 0L;
    }

    private Summary(Double sum, Double max, Double min, Long count) {
        this.sum = sum;
        this.max = max;
        this.min = min;
        this.count = count;
        this.avg = count != 0 ? sum / count : 0;
    }

    public void addObservation(Double value) {
        sum = sum + value;
        count++;
        max = Math.max(value, max);
        min = Math.min(value, min);
        avg = sum / count;
    }

    public Summary mergedSummary(Summary anotherSummary) {
        return new Summary(sum + anotherSummary.sum,
                Math.max(max, anotherSummary.max),
                Math.min(min, anotherSummary.min),
                count + anotherSummary.count);
    }

    public Double getSum() {
        return sum;
    }

    public Double getAvg() {
        return avg;
    }

    public Double getMax() {
        return max;
    }

    public Double getMin() {
        return min;
    }

    public Long getCount() {
        return count;
    }
}

