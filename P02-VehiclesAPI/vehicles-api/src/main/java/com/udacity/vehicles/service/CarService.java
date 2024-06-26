package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final MapsClient mapsClient;
    private final PriceClient pricingClient;

    public CarService(CarRepository repository,
                      MapsClient mapsClient,
                      PriceClient pricingClient) {
        this.repository = repository;
        this.mapsClient = mapsClient;
        this.pricingClient = pricingClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        // Fetch the car from the repository
        Car car = repository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with id " + id + " does not exist"));

        // Fetch the price using the PriceClient and set it on the car
        String carPrice = pricingClient.getPrice(id);
        car.setPrice(carPrice);

        // Fetch the location using the MapsClient and set it on the car
        Location carLocation = mapsClient.getAddress(car.getLocation());
        car.setLocation(carLocation);

        return car;
    }



    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /*
           Find the car by ID from the `repository` if it exists.
           If it does not exist, throw a CarNotFoundException
         */
        repository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with id " + id + " does not exist"));

        // Delete the car from the repository.
        repository.deleteById(id);
    }
}
