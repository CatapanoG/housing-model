package data;

import housing.Model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.random.MersenneTwister;

import utilities.BinnedData;
import utilities.BinnedDataDouble;
import utilities.Pdf;

/**************************************************************************************************
 * Class to read and work with income data before passing it to the Household class. Note that we
 * use here income to refer to gross employment income.
 *
 * @author daniel, Adrian Carro, Gennaro Catapano
 *
 *************************************************************************************************/
public class EmploymentIncome {

    //------------------//
    //----- Fields -----//
    //------------------//
	
	//GC:
	//Store the income-age data:
	static private double[][] incomeAge = loadGrossEmploymentIncomeGivenAge();
	static public MersenneTwister prng;

    /***
     * Calibrated against LCFS 2012 data
     */
    static private BinnedData<Pdf> lnIncomeGivenAge = loadGrossEmploymentIncomePDFGivenAge();

    //-------------------//
    //----- Methods -----//
    //-------------------//
    
    /**
     *  GC:
     * 
     */
	static private double[][] loadGrossEmploymentIncomeGivenAge() {
		
		double[][] data = new double[112][];
		
		Iterator<CSVRecord> records;
		try {
			Reader in = new FileReader(Model.config.DATA_INCOME_GIVEN_AGE);
			records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
			
			CSVRecord record;
			for(int i = 0; i < 112; i++)
			{
				data[i] = new double[5];
				record = records.next();
				
				for(int j = 0; j < 5; j++)
				{
					data[i][j] = Double.parseDouble(record.get(j));
					if (j == 2 || j == 3)
					{
						data[i][j] = Math.exp(data[i][j]);
					}
					if (j == 4 && i % 16 != 0)
					{
						data[i][j] += data[i-1][j];
						if(data[i][j] >= 0.9999)
						{
							data[i][j] = 1;
						}
					}
					//System.out.println(data[i][j]);
				}
			}
		} catch (IOException e) {
			System.out.println("Error loading data for income given age in data.EmploymentIncome");
			e.printStackTrace();
		}
		
		return data;
	}
	
    /**
     *  GC:
     * 
     */
	private static double getIncomeGivenAgeAndPercentile(double _age, double _percentile)
	{
		double income = 0.0;
		
		for (int i = 0; i < incomeAge.length; i++)
		{
			double minAge = incomeAge[i][0];
			double maxAge = incomeAge[i][1];
			double percentile = incomeAge[i][4];
			if (_age > minAge && _age <= maxAge + 1 && _percentile <= percentile)
			{
				double minIncome = incomeAge[i][2];
				double maxIncome = incomeAge[i][3]; 
				double rnd = prng.nextDouble();
				income = minIncome + rnd * (maxIncome - minIncome);
				
				/*
				double rnd = Math.random();
				if (rnd < 0.17)
				{
					income *= 0.05;
				} else if (rnd > 0.90)
				{
					income *= 1.95;
				}
				*/
			
				break;
			}
		}
		
		return income;
	}

    /**
     * Read data from file Model.config.DATA_INCOME_GIVEN_AGE and return it as a binnedData pdf of gross employment
     * income conditional on household age. Note that we are dealing here with logarithmic incomes.
     */
	static private BinnedData<Pdf> loadGrossEmploymentIncomePDFGivenAge() {
		final int givenMinCol = 0;
		final int givenMaxCol = 1;
		final int varMinCol = 2;
		final int varMaxCol = 3;
		final int probCol = 4;
		BinnedData<Pdf> data = new BinnedData<>(0.0, 0.0);
		BinnedData<BinnedDataDouble> pdfData = new BinnedData<>(0.0,0.0);
		BinnedDataDouble pdf;
		double pdfBinMin;
		double pdfBinWidth;
		double lastBinMin;
		
		Iterator<CSVRecord> records;
		try {
			Reader in = new FileReader(Model.config.DATA_INCOME_GIVEN_AGE);
			records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
			
			CSVRecord record;
			if (records.hasNext()) {
				record = records.next();
				
				//GC: how to get data:
				//System.out.println(record.get(0));
				
                data.setFirstBinMin(Double.valueOf(record.get(givenMinCol)));
			    data.setBinWidth(Double.valueOf(record.get(givenMaxCol)) - data.getSupportLowerBound());
			    pdfBinMin = Double.valueOf(record.get(varMinCol));
			    pdfBinWidth = Double.valueOf(record.get(varMaxCol)) - pdfBinMin;
			    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
			    pdfData.add(pdf);
			    pdf.add(Double.valueOf(record.get(probCol)));
			    lastBinMin = data.getSupportLowerBound();
			    while (records.hasNext()) {
			    	record = records.next();
			    	if (Double.valueOf(record.get(givenMinCol)) != lastBinMin) {
					    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
					    pdfData.add(pdf);
					    lastBinMin = Double.valueOf(record.get(givenMinCol));
			    	}
		    		pdf.add(Double.valueOf(record.get(probCol)));
			    }
				for (BinnedDataDouble d: pdfData) {
					data.add(new Pdf(d));
				}
			}
		} catch (IOException e) {
			System.out.println("Error loading data for income given age in data.EmploymentIncome");
			e.printStackTrace();
		}
		return data;
	}

    /**
     * Find household annual gross income given age and income percentile
     */
    static public double getAnnualGrossEmploymentIncome(double boundAge, double incomePercentile) {
        // If boundAge is below minimum age bin, then minimum age bin is assigned
        if (boundAge < lnIncomeGivenAge.getSupportLowerBound()) {
            boundAge = lnIncomeGivenAge.getSupportLowerBound();
        }
        // If boundAge is above maximum age bin, then maximum age bin is assigned
        else if (boundAge > lnIncomeGivenAge.getSupportUpperBound()) {
            boundAge = lnIncomeGivenAge.getSupportUpperBound() - 1e-7;
        }
        // Assign gross annual income according to the determined boundAge
        
        // GC: old way to compute the income:
        //double income = Math.exp(lnIncomeGivenAge.getBinAt(boundAge).inverseCumulativeProbability(incomePercentile));
        //System.out.println("Age: " + boundAge + ". Perc: " + incomePercentile);
        //System.out.println("");
        
        // GC: new (mine) way to compute the income:
        double income = getIncomeGivenAgeAndPercentile(boundAge, incomePercentile);
        //System.out.println("Age: " + boundAge + ". Perc: " + incomePercentile + " incomeNew : " + incomeZ + " incomeOld: " + income);
        
        // Impose a minimum income equivalent to the minimum government annual income support
        if (income < Model.config.GOVERNMENT_MONTHLY_INCOME_SUPPORT*Model.config.constants.MONTHS_IN_YEAR) {
            income = Model.config.GOVERNMENT_MONTHLY_INCOME_SUPPORT*Model.config.constants.MONTHS_IN_YEAR;
        }
        return income;
    }
    
    // GC: start
    static public void permanently_shock_incomeAge(float shock_size)
    {
		for (int i = 0; i < incomeAge.length; i++)
		{
			incomeAge[i][2] *= shock_size;
			incomeAge[i][3] *= shock_size;
		}
    
    	return;
    }
    // GC: end
    
}
