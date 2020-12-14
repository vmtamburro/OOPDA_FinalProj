import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * Write a description of class Coyote here.
 *
 * @author (Victoria Tamburro)
 * @version 2020.11.29 (1)
 */
public class Coyote extends Animal
{
    // Characteristics shared by all coyotes (class variables).
    
    // The age at which a coyote can start to breed.
    private static final int BREEDING_AGE = 15;
    // The age to which a coyote can live.
    private static final int MAX_AGE = 150;
    // The likelihood of a coyote breeding.
    private static final double BREEDING_PROBABILITY = 0.062;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 2;
    // The food value of a single rabbit. In effect, this is the
    // number of steps a coyote can go before it has to eat again.
    private static final int RABBIT_FOOD_VALUE = 9;
    // The food value of a single fox. In effect, this is the
    // number of steps a coyote can go before it has to eat again.
    private static final int FOX_FOOD_VALUE = 20;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).
    // The coyote's age.
    private int age;
    // The coyote's food level, which is increased by eating rabbits and foxes.
    private int foodLevel;

    /**
     * Constructor for objects of class Coyote
     */
    public Coyote(boolean randomAge, Field field, Location location)
    {
        super(field, location);
        if(randomAge){
            age = rand.nextInt(MAX_AGE);
            int foodvalue = (RABBIT_FOOD_VALUE + FOX_FOOD_VALUE) / 2;
            foodLevel = rand.nextInt(foodvalue);
        }
        else{
            age = 0;
            foodLevel = (FOX_FOOD_VALUE + RABBIT_FOOD_VALUE) / 2;
        }
    }


    /**
     * This is what the coyote does most of the time: it hunts for
     * rabbits or foxes. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newCoyotes A list to return newly born coyotes.
     */
    public void act(List<Animal> newCoyotes)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newCoyotes);            
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { 
                // No food found - try to move to a free location.
                newLocation = getField().freeAdjacentLocation(getLocation());
            }
            // See if it was possible to move.
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Increase the age. This could result in the coyote's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this coyote more hungry. This could result in the coyote's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for rabbits or foxes adjacent to the current location.
     * Only the first live rabbit is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                    foodLevel = RABBIT_FOOD_VALUE;
                    return where;
                }
            }
            if(animal instanceof Fox){
                Fox fox = (Fox) animal;
                if(fox.isAlive()){
                    fox.setDead();
                    foodLevel = FOX_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this coyote is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newCoyotes A list to return newly born coyotes.
     */
    private void giveBirth(List<Animal> newCoyotes)
    {
        // New coyotes are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Coyote young = new Coyote(false, field, loc);
            newCoyotes.add(young);
        }
    }
  
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A coyote can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }

}
