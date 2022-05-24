package plainplaying.plugin.gens;

public class Pair<type1,type2> {
    type1 _A;
    type2 _B;
    public Pair(type1 A, type2 B){
        this._A = A;
        this._B = B;
    }
    public Pair(){
        this._A = null;
        this._B = null;
    }
    public type1 getA(){
        return this._A;
    }
    public type2 getB(){
        return this._B;
    }
    public void setA(type1 A){
        this._A = A;
    }
    public void setB(type2 B){
        this._B = B;
    }
    public void set(type1 A, type2 B){
        this._A = A;
        this._B = B;
    }
}
