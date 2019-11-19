package housing;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;

import collectors.*;
import data.EmploymentIncome;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

/**************************************************************************************************
 * This is the root object of the simulation. Upon creation it creates and initialises all the
 * agents in the model.
 *
 * The project is prepared to be run with maven, and it takes the following command line input
 * arguments:
 *
 * -configFile <arg>    Configuration file to be used (address within project folder). By default,
 *                      'src/main/resources/config.properties' is used.
 * -outputFolder <arg>  Folder in which to collect all results (address within project folder). By
 *                      default, 'Results/<current date and time>/' is used. The folder will be
 *                      created if it does not exist.
 * -dev                 Removes security question before erasing the content inside output folder
 *                      (if the folder already exists).
 * -help                Print input arguments usage information.
 *
 * Note that the seed for random number generation is set from the config file.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/

public class Model {

    //------------------//
    //----- Fields -----//
    //------------------//

    public static Config                config;
    public static Construction		    construction;
    public static CentralBank		    centralBank;
    public static Bank 				    bank;
    public static HouseSaleMarket       houseSaleMarket;
    public static HouseRentalMarket     houseRentalMarket;
    public static ArrayList<Household>  households;
    public static CreditSupply          creditSupply;
    public static CoreIndicators        coreIndicators;
    public static HouseholdStats        householdStats;
    public static HousingMarketStats    housingMarketStats;
    public static RentalMarketStats     rentalMarketStats;
    public static MicroDataRecorder     transactionRecorder;
    public static int	                nSimulation; // To keep track of the simulation number
    public static int	                t; // To keep track of time (in months)

    static Government		            government;

    private static MersenneTwister      prng;
    private static Demographics		    demographics;
    private static Recorder             recorder;
    private static String               configFileName;
    private static String               outputFolder;

    // +++
    private static double[]				AvRentPrice;
    private static double[]             debtToIncome;
    private static double[]             ooLTI;
    private static double[]             btlLTV;
    private static double[]             ooDebtToIncome;
    private static double[]             mortgageApprovals;
    private static double[]             dtiBorrowers;
    private static double[]             priceToIncome;
    private static double[]             rentalYield;
    private static double[]             interestRateSpread;
    private static double[]             nHomeless;
    private static double[]             nRenting;
    private static double[]             nOwnerOccupier;
    private static double[]             nBTL;
    private static double[]             nNonBTLBankruptcies;
    private static double[]             nBTLBankruptcies;
    private static double[]             housingStock;
    private static double[]             nNewBuild;
    private static double[]             nEmptyHouses;
    private static double[]             BTLStockFraction;
    private static double[]             nRegisteredMortgages;
    private static double[]             bankBalAll;
    private static double[]             BankBalBTL;
    private static double[]             BankBalOO;
    private static double[]             BankBalRent;
    private static double[]             BankBalHomeless;
    private static double[]             ActiveBTLAnnualisedTotalIncome;
    private static double[]             OwnerOccupierAnnualisedTotalIncome;
    private static double[]             RentingAnnualisedTotalIncome;
    private static double[]             HomelessAnnualisedTotalIncome;
    private static double[]             AllAnnualisedTotalIncome;
    private static double[]             ooLTV;
    private static double[]             AvBidPrice;
    private static double[]             AvOfferPrice;
    private static double[]             AvSalePrice;
    private static double[]             AvDaysOnMarket;
    private static double[]             nBuyers;
    private static double[]             nBTLBuyers;
    private static double[]             nSellers;
    private static double[]             nSales;
    private static double[]             supplyVal;
    private static double[]				nFTBMortgages;
    private static double[]				nBTLMortgages;
	private static double[]				totalBTLCredit;
	private static double[]				totalOOCredit;
	private static double[]				avgDownpayment;
	private static double[]             ooDSR;
	private static double[]				btlDSR;
	private static double[]             q1SalePrice;
	private static double[]             q2SalePrice;
	private static double[]             q3SalePrice;
	private static double[]             q4SalePrice;
	private static double[]             q1RentPrice;
	private static double[]             q2RentPrice;
	private static double[]             q3RentPrice;
	private static double[]             q4RentPrice;
	private static double[]             q1SaleN;
	private static double[]             q2SaleN;
	private static double[]             q3SaleN;
	private static double[]             q4SaleN;
	private static double[]             q1RentN;
	private static double[]             q2RentN;
	private static double[]             q3RentN;
	private static double[]             q4RentN;
	private static double[]             q1Princ;
	private static double[]             q2Princ;
	private static double[]             q3Princ;
	private static double[]             q4Princ;
	private static double[]             q1Install;
	private static double[]             q2Install;
	private static double[]             q3Install;
	private static double[]             q4Install;
	private static double[]             q1NumSaleOffers;
	private static double[]             q2NumSaleOffers;
	private static double[]             q3NumSaleOffers;
	private static double[]             q4NumSaleOffers;
	private static double[]             q1MinSaleOffers;
	private static double[]             q2MinSaleOffers;
	private static double[]             q3MinSaleOffers;
	private static double[]             q4MinSaleOffers;
	private static double[]             q1MaxSaleOffers;
	private static double[]             q2MaxSaleOffers;
	private static double[]             q3MaxSaleOffers;
	private static double[]             q4MaxSaleOffers;
	private static double[]             q1AvgSaleOffers;
	private static double[]             q2AvgSaleOffers;
	private static double[]             q3AvgSaleOffers;
	private static double[]             q4AvgSaleOffers;
	
    
    private static int					rndSeed;
    private static boolean              writeCSV; 
          
    private static double[][]           results;
    
    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /**
     * @param configFileName String with the address of the configuration file
     * @param outputFolder String with the address of the folder for storing results
     */
    public Model(String _configFileName, String _outputFolder, int _rndSeed) {
    	
    	writeCSV = false;
        
    	configFileName = _configFileName;
    	outputFolder = _outputFolder;
    	rndSeed = _rndSeed;
        
    	config = new Config(configFileName);
        prng = new MersenneTwister(rndSeed);
    }
    
    public static void ModelInit()
    {	
        government = new Government();
        demographics = new Demographics(prng);
        construction = new Construction(prng);
        centralBank = new CentralBank();
        bank = new Bank();
        households = new ArrayList<>(config.TARGET_POPULATION*2);
        houseSaleMarket = new HouseSaleMarket(prng);
        houseRentalMarket = new HouseRentalMarket(prng);
        
        EmploymentIncome.prng = prng;

        if(writeCSV == true)
        {
        	recorder = new collectors.Recorder(outputFolder);
        }
        transactionRecorder = new collectors.MicroDataRecorder(outputFolder);
        creditSupply = new collectors.CreditSupply(outputFolder);
        coreIndicators = new collectors.CoreIndicators();
        householdStats = new collectors.HouseholdStats();
        housingMarketStats = new collectors.HousingMarketStats(houseSaleMarket);
        rentalMarketStats = new collectors.RentalMarketStats(housingMarketStats, houseRentalMarket);

        nSimulation = 0;

        // +++
        AvRentPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];	
        debtToIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        ooLTI = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        btlLTV = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        ooDebtToIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        mortgageApprovals = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        dtiBorrowers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        priceToIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        rentalYield = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        interestRateSpread = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nHomeless = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nRenting = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nOwnerOccupier = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nBTL = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nNonBTLBankruptcies = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nBTLBankruptcies = new double[config.N_STEPS - config.TIME_TO_START_RECORDING]; 
        housingStock = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nNewBuild = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nEmptyHouses = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        BTLStockFraction = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nRegisteredMortgages = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        bankBalAll = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        BankBalBTL = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        BankBalOO = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        BankBalRent = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        BankBalHomeless = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        ActiveBTLAnnualisedTotalIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        OwnerOccupierAnnualisedTotalIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        RentingAnnualisedTotalIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        HomelessAnnualisedTotalIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        AllAnnualisedTotalIncome = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        ooLTV = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];      
        AvBidPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        AvOfferPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        AvSalePrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        AvDaysOnMarket = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nBuyers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nBTLBuyers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nSellers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nSales = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        supplyVal = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nFTBMortgages = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
        nBTLMortgages = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	totalBTLCredit = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	totalOOCredit = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	avgDownpayment = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	ooDSR = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	btlDSR = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1SalePrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2SalePrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3SalePrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4SalePrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1RentPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2RentPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3RentPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4RentPrice = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1SaleN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2SaleN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3SaleN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4SaleN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1RentN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2RentN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3RentN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4RentN = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1Princ = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2Princ = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3Princ = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4Princ = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1Install = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2Install = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3Install = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4Install = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1NumSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2NumSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3NumSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4NumSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1MinSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2MinSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3MinSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4MinSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1MaxSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2MaxSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3MaxSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4MaxSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q1AvgSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q2AvgSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q3AvgSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];
    	q4AvgSaleOffers = new double[config.N_STEPS - config.TIME_TO_START_RECORDING];  	
    	
        results = new double[88][config.N_STEPS - config.TIME_TO_START_RECORDING];
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //public static double[][] exec(int rndSeed, 
	public static byte[] exec(int rndSeed, 
    		double market_average_price_decay,
    		double p_fundamentalist,
    		double hpa_expectation_factor,
    		double fundamentalist_cap_gain_coefficient,
    		double trend_cap_gain_coefficient,
    		double hpa_years_to_check,
    		double desired_rent_income_fraction,
    		double psychological_cost_of_renting,
    		double sensitivity_rent_or_purchase,
    		double bank_balance_for_cash_downpayment,
    		double buy_scale,
    		double desired_bank_balance_beta,
    		double decision_to_sell_alpha,
    		double btl_choice_intensity) {
        // the model has about 70 parameters

    	System.out.println("W l'Italia");
    	
        // Handle input arguments from command line
        // handleInputArguments(args);
        String[] args2 = {"args"};
        handleInputArguments(args2);

        // Create an instance of Model in order to initialise it (reading config file)
        Model model = new Model(configFileName, outputFolder, rndSeed);

        /*
         * modify config parameters, if we received any - START
         * this has to be done for each parameter of the exec() function
         */
        
        if (market_average_price_decay != -1.0) config.MARKET_AVERAGE_PRICE_DECAY = market_average_price_decay;
        if (p_fundamentalist != -1.0) config.P_FUNDAMENTALIST = p_fundamentalist;
        if (hpa_expectation_factor != -1.0) config.HPA_EXPECTATION_FACTOR = hpa_expectation_factor;
        if (fundamentalist_cap_gain_coefficient != -1.0) config.FUNDAMENTALIST_CAP_GAIN_COEFF = fundamentalist_cap_gain_coefficient;
        if (trend_cap_gain_coefficient != -1.0) config.TREND_CAP_GAIN_COEFF = trend_cap_gain_coefficient;
        if (hpa_years_to_check != 0) config.HPA_YEARS_TO_CHECK = (int)hpa_years_to_check;
        if (desired_rent_income_fraction != -1.0) config.DESIRED_RENT_INCOME_FRACTION = desired_rent_income_fraction;
        if (psychological_cost_of_renting != -1.0) config.PSYCHOLOGICAL_COST_OF_RENTING = psychological_cost_of_renting;
        if (sensitivity_rent_or_purchase != -1.0) config.SENSITIVITY_RENT_OR_PURCHASE = sensitivity_rent_or_purchase;
        if (bank_balance_for_cash_downpayment != -1.0) config.BANK_BALANCE_FOR_CASH_DOWNPAYMENT = bank_balance_for_cash_downpayment;
        if (buy_scale != -1.0) config.BUY_SCALE = buy_scale;
        if (desired_bank_balance_beta != -1.0) config.DESIRED_BANK_BALANCE_BETA = desired_bank_balance_beta; 
        if (decision_to_sell_alpha != -1.0) config.DECISION_TO_SELL_ALPHA = decision_to_sell_alpha;
        if (btl_choice_intensity != -1.0) config.BTL_CHOICE_INTENSITY = btl_choice_intensity;
        
        config.setDerivedParams();

        // modify config parameters, if we received any - END

        // Call the was-constructor
        ModelInit();
        
        // Start data recorders for output
        setupStatics();

        // Open files for writing multiple runs results
        if(writeCSV == true)
        {
        	recorder.openMultiRunFiles(config.recordCoreIndicators);
        }
        
        // Perform config.N_SIMS simulations
        for (nSimulation = 1; nSimulation <= config.N_SIMS; nSimulation += 1) {

        	if(writeCSV == true)
        	{
        		// For each simulation, open files for writing single-run results
        		recorder.openSingleRunFiles(nSimulation);
        	}
            
            // For each simulation, initialise both houseSaleMarket and houseRentalMarket variables (including HPI)
            init();

            // For each simulation, run config.N_STEPS time steps
            for (t = 0; t < config.N_STEPS; t += 1) {
            	
            	// *******************************
            	// *******************************
            	// PPP policy
            	// *******************************
            	// *******************************
            	// Policy experiments - last 50yrs
            	if (t == (config.N_STEPS - 12*25))
            	{	
            		//LTV EXPERIMENT
            		bank.setAllLTV(0.8, 0.8, 0.8);
            		
            		//CREDIT SUPPLY EXPERIMENT
            		//config.BANK_CREDIT_SUPPLY_TARGET = 300;
            		
            		//Debt service max policy experiment
            		//config.CENTRAL_BANK_AFFORDABILITY_COEFF = 0.30;
            	}

                // Steps model and stores sale and rental markets bid and offer prices, and their averages, into their
                // respective variables
                modelStep();

                if (t >= config.TIME_TO_START_RECORDING) {
                    // Write results of this time step and run to both multi- and single-run files
                    // ---
                    //recorder.writeTimeStampResults(config.recordCoreIndicators, t);
                    // +++
                	AvRentPrice[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getAvSalePrice();
                    debtToIncome[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getDebtToIncome();
                    ooLTI[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getOwnerOccupierLTIMeanAboveMedian();
                    btlLTV[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getBuyToLetLTVMean();
                    ooDebtToIncome[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getOODebtToIncome();
                    mortgageApprovals[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getMortgageApprovals();
                    dtiBorrowers[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.dtiBorrowers;
                    priceToIncome[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getPriceToIncome();
                    rentalYield[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getAvStockYield();
                    interestRateSpread[t- config.TIME_TO_START_RECORDING] = Model.coreIndicators.getInterestRateSpread();
                    nHomeless[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnHomeless();
                    nRenting[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnRenting();
                    nOwnerOccupier[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnOwnerOccupier();
                    nBTL[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnBTL();
                    nNonBTLBankruptcies[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnNonBTLBankruptcies();
                    nBTLBankruptcies[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnBTLBankruptcies();
                    housingStock[t - config.TIME_TO_START_RECORDING] = Model.construction.getHousingStock();
                    nNewBuild[t - config.TIME_TO_START_RECORDING] = Model.construction.getnNewBuild();
                    nEmptyHouses[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getnEmptyHouses();
                    BTLStockFraction[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getBTLStockFraction();
                    nRegisteredMortgages[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getnRegisteredMortgages();
                    bankBalAll[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getbankBalAll();
                    BankBalBTL[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getBankBalBTL();
                    BankBalOO[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getBankBalOO();
                    BankBalRent[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getBankBalRent();
                    BankBalHomeless[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getBankBalHomeless();
                    ActiveBTLAnnualisedTotalIncome[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getActiveBTLAnnualisedTotalIncome();
                    OwnerOccupierAnnualisedTotalIncome[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getOwnerOccupierAnnualisedTotalIncome();
                    RentingAnnualisedTotalIncome[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getRentingAnnualisedTotalIncome();
                    HomelessAnnualisedTotalIncome[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getHomelessAnnualisedTotalIncome();
                    AllAnnualisedTotalIncome[t - config.TIME_TO_START_RECORDING] = Model.householdStats.getAllAnnualisedTotalIncome();
                    ooLTV[t - config.TIME_TO_START_RECORDING] = Model.coreIndicators.getOwnerOccupierLTVMean();
                    AvBidPrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvBidPrice();
                    AvOfferPrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvOfferPrice();
                    AvSalePrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvSalePrice();
                    AvDaysOnMarket[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvDaysOnMarket();
                    nBuyers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getnBuyers();
                    nBTLBuyers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getnBTLBuyers();
                    nSellers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getnSellers();
                    nSales[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getnSales();
                    supplyVal[t - config.TIME_TO_START_RECORDING] = Model.bank.getSupplyVal();
                    nFTBMortgages[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.nFTBMortgages;
                    nBTLMortgages[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.nBTLMortgages;
                	totalBTLCredit[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.totalBTLCredit;
                	totalOOCredit[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.totalOOCredit;
                	avgDownpayment[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvgDownpayment();
                	ooDSR[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.ooDSR;
                	btlDSR[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.btlDSR;
                	q1SalePrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvSalePricePerQualityQuartile(1);
                	q2SalePrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvSalePricePerQualityQuartile(2);
                	q3SalePrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvSalePricePerQualityQuartile(3);
                	q4SalePrice[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvSalePricePerQualityQuartile(4);
                	q1RentPrice[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getAvSalePricePerQualityQuartile(1);
                	q2RentPrice[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getAvSalePricePerQualityQuartile(2);
                	q3RentPrice[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getAvSalePricePerQualityQuartile(3);
                	q4RentPrice[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getAvSalePricePerQualityQuartile(4);
                	q1SaleN[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSalesPerQualityQuartile(1);
                	q2SaleN[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSalesPerQualityQuartile(2);
                	q3SaleN[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSalesPerQualityQuartile(3);
                	q4SaleN[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSalesPerQualityQuartile(4);
                	q1RentN[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getNSalesPerQualityQuartile(1);
                	q2RentN[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getNSalesPerQualityQuartile(2);
                	q3RentN[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getNSalesPerQualityQuartile(3);
                	q4RentN[t - config.TIME_TO_START_RECORDING] = Model.rentalMarketStats.getNSalesPerQualityQuartile(4);
                	q1Princ[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvPrincipalByQualityQuartile(1);
                	q2Princ[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvPrincipalByQualityQuartile(2);
                	q3Princ[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvPrincipalByQualityQuartile(3);
                	q4Princ[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvPrincipalByQualityQuartile(4);
                	q1Install[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvInstallByQualityQuartile(1);
                	q2Install[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvInstallByQualityQuartile(2);
                	q3Install[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvInstallByQualityQuartile(3);
                	q4Install[t - config.TIME_TO_START_RECORDING] = Model.creditSupply.getAvInstallByQualityQuartile(4);
                	q1NumSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSaleOffersPerQualityQuartile(1);
                	q2NumSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSaleOffersPerQualityQuartile(2);
                	q3NumSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSaleOffersPerQualityQuartile(3);
                	q4NumSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getNSaleOffersPerQualityQuartile(4);
                	q1MinSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMinOfferPerQualityQuartile(1);
                	q2MinSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMinOfferPerQualityQuartile(2);
                	q3MinSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMinOfferPerQualityQuartile(3);
                	q4MinSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMinOfferPerQualityQuartile(4);
                	q1MaxSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMaxOfferPerQualityQuartile(1);
                	q2MaxSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMaxOfferPerQualityQuartile(2);
                	q3MaxSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMaxOfferPerQualityQuartile(3);
                	q4MaxSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getMaxOfferPerQualityQuartile(4);
                	q1AvgSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvgOfferPerQualityQuartile(1);
                	q2AvgSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvgOfferPerQualityQuartile(2);
                	q3AvgSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvgOfferPerQualityQuartile(3);
                	q4AvgSaleOffers[t - config.TIME_TO_START_RECORDING] = Model.housingMarketStats.getAvgOfferPerQualityQuartile(4);                	
                }

                // Print time information to screen
                if (t % 100 == 0) {
                    System.out.println("Simulation: " + nSimulation + ", time: " + t);
                }
            }

            // Finish each simulation within the recorders (closing single-run files, changing line in multi-run files)
            // ---
            //recorder.finishRun(config.recordCoreIndicators);
            // TODO: Check what this is actually doing and if it is necessary
            // ---
            ///if(config.recordMicroData) transactionRecorder.endOfSim();
        }
        
    	results[0] = AvRentPrice;
    	results[1] = debtToIncome;
    	results[2] = ooLTI;
    	results[3] = btlLTV;
    	results[4] = ooDebtToIncome;
    	results[5] = mortgageApprovals;
    	results[6] = dtiBorrowers;
    	results[7] = priceToIncome;
    	results[8] = rentalYield;
    	results[9] = interestRateSpread;
    	results[10] = nHomeless;
    	results[11] = nRenting;
    	results[12] = nOwnerOccupier;
    	results[13] = nBTL;
    	results[14] = nNonBTLBankruptcies;
    	results[15] = nBTLBankruptcies;
    	results[16] = housingStock;
        results[17] = nNewBuild;
        results[18] = nEmptyHouses;
        results[19] = BTLStockFraction;
        results[20] = nRegisteredMortgages;
        results[21] = bankBalAll;
        results[22] = BankBalBTL;
        results[23] = BankBalOO;
        results[24] = BankBalRent;
        results[25] = BankBalHomeless;
        results[26] = ActiveBTLAnnualisedTotalIncome;
        results[27] = OwnerOccupierAnnualisedTotalIncome;
        results[28] = RentingAnnualisedTotalIncome;
        results[29] = HomelessAnnualisedTotalIncome;
        results[30] = AllAnnualisedTotalIncome;
        results[31] = ooLTV;
        results[32] = AvBidPrice;
        results[33] = AvOfferPrice;
        results[34] = AvSalePrice;
        results[35] = AvDaysOnMarket;
        results[36] = nBuyers;
        results[37] = nBTLBuyers;
        results[38] = nSellers;
        results[39] = nSales;
        results[40] = supplyVal;
        results[41] = nFTBMortgages;
        results[42] = nBTLMortgages;
    	results[43] = totalBTLCredit;
    	results[44] = totalOOCredit;
    	results[45] = avgDownpayment;
    	results[46] = ooDSR;
    	results[47] = btlDSR;
    	results[48] = q1SalePrice;
    	results[49] = q2SalePrice;
    	results[50] = q3SalePrice;
    	results[51] = q4SalePrice;
    	results[52] = q1RentPrice;
    	results[53] = q2RentPrice;
    	results[54] = q3RentPrice;
    	results[55] = q4RentPrice;
    	results[56] = q1SaleN;
    	results[57] = q2SaleN;
    	results[58] = q3SaleN;
    	results[59] = q4SaleN;
    	results[60] = q1RentN;
    	results[61] = q2RentN;
    	results[62] = q3RentN;
    	results[63] = q4RentN;
    	results[64] = q1Princ;
    	results[65] = q2Princ;
    	results[66] = q3Princ;
    	results[67] = q4Princ;
    	results[68] = q1Install;
    	results[69] = q2Install;
    	results[70] = q3Install;
    	results[71] = q4Install;
    	results[72] = q1NumSaleOffers;
    	results[73] = q2NumSaleOffers;
    	results[74] = q3NumSaleOffers;
    	results[75] = q4NumSaleOffers;
    	results[76] = q1MinSaleOffers;
    	results[77] = q2MinSaleOffers;
    	results[78] = q3MinSaleOffers;
    	results[79] = q4MinSaleOffers;
    	results[80] = q1MaxSaleOffers;
    	results[81] = q2MaxSaleOffers;
    	results[82] = q3MaxSaleOffers;
    	results[83] = q4MaxSaleOffers;
    	results[84] = q1AvgSaleOffers;
    	results[85] = q2AvgSaleOffers;
    	results[86] = q3AvgSaleOffers;
    	results[87] = q4AvgSaleOffers;
    	
    	byte[] results2;
    	
    	java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    	java.io.ObjectOutput out = null;
    	
    	try {
    		out = new java.io.ObjectOutputStream(bos);
    		out.writeObject(results);
    		out.flush();
    		results2 = bos.toByteArray();
    		bos.close();   		
    	} catch (IOException ex) {
    		// nothing
    		results2 = new byte[1];
    	}

    	return results2;
    	
        // After the last simulation, clean up
        // ---
        //recorder.finish(config.recordCoreIndicators);
        //if(config.recordMicroData) transactionRecorder.finish();

        // Stop the program when finished
        // ---
        // System.exit(0);
    }

    public static void main(String[] args) {
        /* Let's call exec() passing only null parameters.
         *
         * When exec() receives a null parameter, it uses the default one from
         * the config file, thus executing main() becomes equivalent to running
         * with no parameter overriding.
         */
        System.out.println("GC INFO: running the simulation with default parameters");
        exec(0, -1.0, -1.0, -1.0, -1.0, -1.0, 0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0);
    }

    private static void setupStatics() {
        setRecordGeneral();
        setRecordCoreIndicators(config.recordCoreIndicators);
        setRecordMicroData(config.recordMicroData);
    }

    private static void init() {
        construction.init();
        houseSaleMarket.init();
        houseRentalMarket.init();
        bank.init();
        centralBank.init();
        housingMarketStats.init();
        rentalMarketStats.init();
        householdStats.init();
        households.clear();
    }

    private static void modelStep() {
        // Update population with births and deaths
        demographics.step();
        // Update number of houses
        construction.step();
        // Updates regional households consumption, housing decisions, and corresponding regional bids and offers
        for(Household h : households) h.step();
        // Stores sale market bid and offer prices and averages before bids are matched by clearing the market
        housingMarketStats.preClearingRecord();
        // Clears sale market and updates the HPI
        houseSaleMarket.clearMarket();
        // Computes and stores several housing market statistics after bids are matched by clearing the market (such as HPI, HPA)
        housingMarketStats.postClearingRecord();
        // Stores rental market bid and offer prices and averages before bids are matched by clearing the market
        rentalMarketStats.preClearingRecord();
        // Clears rental market
        houseRentalMarket.clearMarket();
        // Computes and stores several rental market statistics after bids are matched by clearing the market (such as HPI, HPA)
        rentalMarketStats.postClearingRecord();
        // Stores household statistics after both regional markets have been cleared
        householdStats.record();
        // Update credit supply statistics // TODO: Check what this actually does and if it should go elsewhere!
        creditSupply.step();
        // Update bank and interest rate for new mortgages
        bank.step(Model.households.size());
        // Update central bank policies (currently empty!)
        centralBank.step(coreIndicators);
    }

    /**
     * This method handles command line input arguments to
     * determine the address of the input config file and
     * the folder for outputs
     *
     * @param args String with the command line arguments
     */
    private static void handleInputArguments(String[] args) {

        // Create Options object
        Options options = new Options();

        // Add configFile and outputFolder options
        options.addOption("configFile", true, "Configuration file to be used (address within " +
                "project folder). By default, 'src/main/resources/config.properties' is used.");
        options.addOption("outputFolder", true, "Folder in which to collect all results " +
                "(address within project folder). By default, 'Results/<current date and time>/' is used. The " +
                "folder will be created if it does not exist.");
        options.addOption("dev", false, "Removes security question before erasing the content" +
                "inside output folder (if the folder already exists).");
        options.addOption("help", false, "Print input arguments usage information.");

        // Create help formatter in case it will be needed
        HelpFormatter formatter = new HelpFormatter();

        // Parse command line arguments and perform appropriate actions
        // Create a parser and a boolean variable for later control
        CommandLineParser parser = new DefaultParser();
        boolean devBoolean = false;
        try {
            // Parse command line arguments into a CommandLine instance
            CommandLine cmd = parser.parse(options, args);
            // Check if help argument has been passed
            if(cmd.hasOption("help")) {
                // If it has, then print formatted help to screen and stop program
                formatter.printHelp( "spatial-housing-model", options );
                System.exit(0);
            }
            // Check if dev argument has been passed
            if(cmd.hasOption("dev")) {
                // If it has, then activate boolean variable for later control
                devBoolean = true;
            }
            // Check if configFile argument has been passed
            if(cmd.hasOption("configFile")) {
                // If it has, then use its value to initialise the respective member variable
                configFileName = cmd.getOptionValue("configFile");
            } else {
                // If not, use the default value to initialise the respective member variable
                configFileName = "src/main/resources/config.properties";
            }
            // Check if outputFolder argument has been passed
            if(cmd.hasOption("outputFolder")) {
                // If it has, then use its value to initialise the respective member variable
                outputFolder = cmd.getOptionValue("outputFolder");
                // If outputFolder does not end with "/", add it
                if (!outputFolder.endsWith("/")) { outputFolder += "/"; }
            } else {
                // If not, use the default value to initialise the respective member variable
                outputFolder = "Results/" + Instant.now().toString().replace(":", "-") + "/";
            }
        }
        catch(ParseException pex) {
            // Catch possible parsing errors
            System.err.println("Parsing failed. Reason: " + pex.getMessage());
            // And print input arguments usage information
            formatter.printHelp( "spatial-housing-model", options );
        }
        
        if (writeCSV) {
	        // Check if outputFolder directory already exists
	        File f = new File(outputFolder);
	        if (f.exists() && !devBoolean) {
	            // If it does, try removing everything inside (with a warning that requests approval!)
	            Scanner reader = new Scanner(System.in);
	            System.out.println("\nATTENTION:\n\nThe folder chosen for output, '" + outputFolder + "', already exists and " +
	                    "might contain relevant files.\nDo you still want to proceed and erase all content?");
	            String reply = reader.next();
	            if (!reply.equalsIgnoreCase("yes") && !reply.equalsIgnoreCase("y")) {
	                // If user does not clearly reply "yes", then stop the program
	                System.exit(0);
	            } else {
	                // Otherwise, try to erase everything inside the folder
	                try {
	                    FileUtils.cleanDirectory(f);
	                } catch (IOException ioe) {
	                    // Catch possible folder cleaning errors
	                    System.err.println("Folder cleaning failed. Reason: " + ioe.getMessage());
	                }
	            }
	        } else {
	            // If it doesn't, simply create it
	            f.mkdirs();
	        }
	
	        // Copy config file to output folder
	        try {
	            FileUtils.copyFileToDirectory(new File(configFileName), new File(outputFolder));
	        } catch (IOException ioe) {
	            System.err.println("Copying config file to output folder failed. Reason: " + ioe.getMessage());
	        }
        }
    }

    /**
     * @return Simulated time in months
     */
    static public int getTime() { return t; }

    /**
     * @return Current month of the simulation
     */
    static public int getMonth() { return t%12 + 1; }

    public MersenneTwister getPrng() { return prng; }

    private static void setRecordGeneral() {
        creditSupply.setActive(true);
        householdStats.setActive(true);
        housingMarketStats.setActive(true);
        rentalMarketStats.setActive(true);
    }

    private static void setRecordCoreIndicators(boolean recordCoreIndicators) {
        coreIndicators.setActive(recordCoreIndicators);
    }

    private static void setRecordMicroData(boolean record) { transactionRecorder.setActive(record); }

}
