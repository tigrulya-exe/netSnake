package nsu.manasyan.netsnake.observable;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
    public interface ValueListener<T>{
        void onUpdate(T value);
    }

    private T value;

    private List<ValueListener<T>> valueListeners = new ArrayList<>();

    public Observable(T value) {
        this.value = value;
    }

    public void registerValueListener(ValueListener<T> valueListener){
        valueListeners.add(valueListener);
    }

    public void updateValue(T newValue){
        value = newValue;
        notifyAllValueListeners();
    }

    private void notifyAllValueListeners(){
        valueListeners.forEach(vl -> vl.onUpdate(value));
    }
}
