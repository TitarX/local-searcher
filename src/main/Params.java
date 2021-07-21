/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author TitarX
 */
public class Params
{

    private String findString = "";
    private String locationString = "";
    private int regexParam = 0;
    private int searchType = 2;

    public String getFindString()
    {
        return findString;
    }

    public void setFindString(String findString)
    {
        this.findString = findString;
    }

    public String getLocationString()
    {
        return locationString;
    }

    public void setLocationString(String locationString)
    {
        this.locationString = locationString;
    }

    public int getRegexParam()
    {
        return regexParam;
    }

    public void setRegexParam(int regexParam)
    {
        this.regexParam = regexParam;
    }

    public int getSearchType()
    {
        return searchType;
    }

    public void setSearchType(int searchType)
    {
        this.searchType = searchType;
    }
}
