package collectors;

import housing.Config;
import housing.Household;
import housing.Model;

/**************************************************************************************************
 * Class to collect regional household statistics
 *
 * @author daniel, Adrian Carro, Gennaro Catapano
 *
 *************************************************************************************************/
public class HouseholdStats extends CollectorBase {

	//------------------//
	//----- Fields -----//
	//------------------//

	// General fields
	private Config  config = Model.config; // Passes the Model's configuration parameters object to a private field

	// Fields for counting numbers of the different types of households and household conditions
	private int     nBTL; // Number of buy-to-let (BTL) households, i.e., households with the BTL gene (includes both active and inactive)
	private int     nActiveBTL; // Number of BTL households with, at least, one BTL property
	private int     nBTLOwnerOccupier; // Number of BTL households owning their home but without any BTL property
	private int     nBTLHomeless; // Number of homeless BTL households
    private int     nBTLBankruptcies; // Number of BTL households going bankrupt in a given time step
	private int     nNonBTLOwnerOccupier; // Number of non-BTL households owning their home
	private int     nRenting; // Number of (by definition, non-BTL) households renting their home
	private int     nNonBTLHomeless; // Number of homeless non-BTL households
    private int     nNonBTLBankruptcies; // Number of non-BTL households going bankrupt in a given time step
    //GC:
    private int     nNonBTLNewBakruptcies; // Num of newly bankrupt non-BTL households
    //GC: end GC

	// Fields for summing annualised total incomes
	private double  activeBTLAnnualisedTotalIncome;
	private double  ownerOccupierAnnualisedTotalIncome;
	private double  rentingAnnualisedTotalIncome;
	private double  homelessAnnualisedTotalIncome;
	private double  allAnnualisedTotalIncome;

	// Other fields
	private double  sumStockYield; // Sum of stock gross rental yields of all currently occupied rental properties
    private int     nNonBTLBidsAboveExpAvSalePrice; // Number of normal (non-BTL) bids with desired housing expenditure above the exponential moving average sale price
    private int     nBTLBidsAboveExpAvSalePrice; // Number of BTL bids with desired housing expenditure above the exponential moving average sale price
    private int     nNonBTLBidsAboveExpAvSalePriceCounter; // Counter for the number of normal (non-BTL) bids with desired housing expenditure above the exp. mov. av. sale price
    private int     nBTLBidsAboveExpAvSalePriceCounter; // Counter for the number of BTL bids with desired housing expenditure above the exp. mov. av. sale price

    // Bank balance
    private double  bankBalBTL;
    private double  bankBalOO;
    private double  bankBalRent;
    private double  bankBalHomeless;
    private double  bankBalAll;
    
	//------------------------//
	//----- Constructors -----//
	//------------------------//

    /**
     * Initialises the household statistics collector
     */
    public HouseholdStats() { setActive(true); }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Sets initial values for all relevant variables to enforce a controlled first measure for statistics
     */
    public void init() {
        nBTL = 0;
        nActiveBTL = 0;
        nBTLOwnerOccupier = 0;
        nBTLHomeless = 0;
        nBTLBankruptcies = 0;
        nNonBTLOwnerOccupier = 0;
        nRenting = 0;
        nNonBTLHomeless = 0;
        nNonBTLBankruptcies = 0;
        activeBTLAnnualisedTotalIncome = 0.0;
        ownerOccupierAnnualisedTotalIncome = 0.0;
        rentingAnnualisedTotalIncome = 0.0;
        homelessAnnualisedTotalIncome = 0.0;
        sumStockYield = 0.0;
        nNonBTLBidsAboveExpAvSalePrice = 0;
        nBTLBidsAboveExpAvSalePrice = 0;
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
        
        bankBalBTL = 0.0;
        bankBalOO = 0.0;
        bankBalRent = 0.0;
        bankBalHomeless = 0.0;
        bankBalAll = 0.0;
        
        allAnnualisedTotalIncome = 0.0;
        
        // GC:
        nNonBTLNewBakruptcies = 0;
        // GC: END
    }

    public void record() {
        // Initialise variables to sum
        nBTL = 0;
        nActiveBTL = 0;
        nBTLOwnerOccupier = 0;
        nBTLHomeless = 0;
        nBTLBankruptcies = 0;
        nNonBTLOwnerOccupier = 0;
        nRenting = 0;
        nNonBTLHomeless = 0;
        nNonBTLBankruptcies = 0;
        activeBTLAnnualisedTotalIncome = 0.0;
        ownerOccupierAnnualisedTotalIncome = 0.0;
        rentingAnnualisedTotalIncome = 0.0;
        homelessAnnualisedTotalIncome = 0.0;
        sumStockYield = 0.0;
        
        bankBalBTL = 0.0;
        bankBalOO = 0.0;
        bankBalRent = 0.0;
        bankBalHomeless = 0.0;
        bankBalAll = 0.0;
        
        // GC:
        nNonBTLNewBakruptcies = 0;
        // GC: END
        
        // Run through all households counting population in each type and summing their gross incomes
        for (Household h : Model.households) {
            if (h.behaviour.isPropertyInvestor()) {
                ++nBTL;
                bankBalBTL += h.getBankBalance();
                if (h.isBankrupt()) nBTLBankruptcies += 1;
                // Active BTL investors
                if (h.nInvestmentProperties() > 0) {
                    ++nActiveBTL;
                    activeBTLAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    // Inactive BTL investors who own their house
                } else if (h.nInvestmentProperties() == 0) {
                    ++nBTLOwnerOccupier;
                    ownerOccupierAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    // Inactive BTL investors in social housing
                } else {
                    ++nBTLHomeless;
                    homelessAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                }
            } else {
            	
            	// GC: check new bankruptcies among non-BTL investors
            	if (h.isBankrupt() == true && h.wasBankrupt() == false)
            	{
            		nNonBTLNewBakruptcies++;
            	}
            	// GC: END
            	
                if (h.isBankrupt()) nNonBTLBankruptcies += 1;
                // Non-BTL investors who own their house
                if (h.isHomeowner()) {
                    ++nNonBTLOwnerOccupier;
                    ownerOccupierAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    bankBalOO += h.getBankBalance();
                    // Non-BTL investors renting
                } else if (h.isRenting()) {
                    ++nRenting;
                    rentingAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    if (Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality()) > 0) {
                        sumStockYield += h.getHousePayments().get(h.getHome()).monthlyPayment
                                *config.constants.MONTHS_IN_YEAR
                                /Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality());
                    }
                    bankBalRent += h.getBankBalance();
                    // Non-BTL investors in social housing
                } else if (h.isInSocialHousing()) {
                	bankBalHomeless = h.getBankBalance();
                    ++nNonBTLHomeless;
                    homelessAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    bankBalHomeless += h.getBankBalance();
                }
            }
        }
        // Annualise monthly income data
        activeBTLAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        ownerOccupierAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        rentingAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        homelessAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        // Pass number of bidders above the exponential moving average sale price to persistent variable and
        // re-initialise to zero the counter
        nNonBTLBidsAboveExpAvSalePrice = nNonBTLBidsAboveExpAvSalePriceCounter;
        nBTLBidsAboveExpAvSalePrice = nBTLBidsAboveExpAvSalePriceCounter;
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
        
        bankBalAll = bankBalBTL + bankBalOO + bankBalRent + bankBalHomeless;
        allAnnualisedTotalIncome = activeBTLAnnualisedTotalIncome + ownerOccupierAnnualisedTotalIncome + rentingAnnualisedTotalIncome + homelessAnnualisedTotalIncome;
    }

    /**
     * Count number of normal (non-BTL) bidders with desired expenditures above the (minimum quality, q=0) exponential
     * moving average sale price
     */
    public void countNonBTLBidsAboveExpAvSalePrice(double price) {
        if (price >= Model.housingMarketStats.getExpAvSalePriceForQuality(0)) {
            nNonBTLBidsAboveExpAvSalePriceCounter++;
        }
    }

    /**
     * Count number of BTL bidders with desired expenditures above the (minimum quality, q=0) exponential moving average
     * sale price
     */
    public void countBTLBidsAboveExpAvSalePrice(double price) {
        if (price >= Model.housingMarketStats.getExpAvSalePriceForQuality(0)) {
            nBTLBidsAboveExpAvSalePriceCounter++;
        }
    }

    //----- Getter/setter methods -----//

    // Getters for numbers of households variables
    public int getnBTL() { return nBTL; }
    public int getnActiveBTL() { return nActiveBTL; }
    public int getnBTLOwnerOccupier() { return nBTLOwnerOccupier; }
    public int getnBTLHomeless() { return nBTLHomeless; }
    public int getnBTLBankruptcies() { return nBTLBankruptcies; }
    public int getnNonBTLOwnerOccupier() { return nNonBTLOwnerOccupier; }
    public int getnRenting() { return nRenting; }
    public int getnNonBTLHomeless() { return nNonBTLHomeless; }
    public int getnNonBTLBankruptcies() { return nNonBTLBankruptcies; }
    public int getnOwnerOccupier() { return nBTLOwnerOccupier + nNonBTLOwnerOccupier; }
    public int getnHomeless() { return nBTLHomeless + nNonBTLHomeless; }
    public int getnNonOwner() { return nRenting + getnHomeless(); }
    // GC:
    public int getnNonBTLNewBakruptcies() { return nNonBTLNewBakruptcies; }
    // GC: END
    
    
    // Getters for annualised income variables
    public double getActiveBTLAnnualisedTotalIncome() { return activeBTLAnnualisedTotalIncome; }
    public double getOwnerOccupierAnnualisedTotalIncome() { return ownerOccupierAnnualisedTotalIncome; }
    public double getRentingAnnualisedTotalIncome() { return rentingAnnualisedTotalIncome; }
    public double getHomelessAnnualisedTotalIncome() { return homelessAnnualisedTotalIncome; }
    public double getNonOwnerAnnualisedTotalIncome() {
        return rentingAnnualisedTotalIncome + homelessAnnualisedTotalIncome;
    }
    public double getAllAnnualisedTotalIncome() { return allAnnualisedTotalIncome; }

    // Getters for yield variables
    public double getSumStockYield() { return sumStockYield; }
    public double getAvStockYield() {
        if(nRenting > 0) {
            return sumStockYield/nRenting;
        } else {
            return 0.0;
        }
    }

    // Getters for other variables...
    // ... number of empty houses (total number of houses minus number of non-homeless households)
    public int getnEmptyHouses() {
        return Model.construction.getHousingStock() + nBTLHomeless + nNonBTLHomeless - Model.households.size();
    }
    // ... proportion of housing stock owned by buy-to-let investors (all rental properties, plus all empty houses not
    // owned by the construction sector)
    public double getBTLStockFraction() {
        return ((double)(getnEmptyHouses() - Model.housingMarketStats.getnUnsoldNewBuild()
                + nRenting))/Model.construction.getHousingStock();
    }
    // ... number of normal (non-BTL) bidders with desired housing expenditure above the exponential moving average sale price
    public int getnNonBTLBidsAboveExpAvSalePrice() { return nNonBTLBidsAboveExpAvSalePrice; }
    // ... number of BTL bidders with desired housing expenditure above the exponential moving average sale price
    public int getnBTLBidsAboveExpAvSalePrice() { return nBTLBidsAboveExpAvSalePrice; }
    
    
    // Get bank balances (cross section sum)
    public double getbankBalAll( ) { return bankBalAll; }
    public double getBankBalBTL() { return bankBalBTL; }
    public double getBankBalOO() { return bankBalOO; }
    public double getBankBalRent() { return bankBalRent; }
    public double getBankBalHomeless() { return bankBalHomeless; }

}
