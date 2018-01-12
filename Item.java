package inventaireenvoyable;

/**
 * A creation of Item instances.
 * @author aparent
 */
public class Item implements java.io.Serializable{
    
    private final String pName;
    private final String pDescription;
    
    /**
     * 
     * @param aName the name
     * @param aDescription the description
     */
    public Item(String aName, String aDescription){
        this.pName = aName;
        this.pDescription = aDescription;
    }
    
    /**
     * 
     * @return the Item's name
     */
    public String getName(){
        String copy = this.pName;
        return copy;
    }
    
    /**
     * 
     * @return the Item's description
     */
    public String getDescription(){
        String copy = this.pDescription;
        return copy;
    }
    
    /**
     * 
     * @param pItem the Item to be compared to
     * @return a boolean describing whether or not the items are identical
     */
    public boolean equals(Item pItem){
        boolean sameName = this.getName().equals(pItem.getName());
        boolean sameDescription = this.getDescription().equals(pItem.getDescription());
        return (sameName && sameDescription);
    }
}