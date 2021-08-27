package plainplaying.plugin.gens;

public class Pair<type1,type2> {
    type1 _A;
    type2 _B;
    public Pair(type1 A, type2 B){
        this._A = A;
        this._B = B;
    }
    public type1 getA(){
        return this._A;
    }
    public type2 getB(){
        return this._B;
    }
}
