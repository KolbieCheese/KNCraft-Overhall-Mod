package com.beautyinblocks.performance;
import java.util.Comparator;
import java.util.List;
public final class NearestSelection {
    private NearestSelection() {}
    // Callers consume ONLY element zero. Preserve the first minimum, as stable sort does.
    public static <T> void moveMinimumFirst(List<T> values, Comparator<? super T> comparator) {
        if (values.size()<2) return;
        int best=0;
        for(int i=1;i<values.size();i++) if(comparator.compare(values.get(i),values.get(best))<0) best=i;
        if(best!=0) { T first=values.get(0); values.set(0,values.get(best)); values.set(best,first); }
    }
}
