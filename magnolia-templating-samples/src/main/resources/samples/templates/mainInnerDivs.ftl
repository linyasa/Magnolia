[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link media="screen" href="${contextPath}/.resources/samples/css/samples.css" type="text/css" rel="stylesheet">
        <title>${content.title!content.@name}</title>
    </head>

    <body>
        <div id="wrapper">

            <cms:page content="website:${content.@path}" dialog="samples:mainProperties" label="/howTo-freemarker - Sample using the NEW freemarker template"></cms:page>

            <div id="header">

                [#-- ****** navigation ****** --]
                [#include "/samples/areas/navigationWrappedWithDiv.ftl" ]
                [#-- should be used as an area so it can be inherited. Inheritance of areas not implemented yet.
                [@cms.area name="navigation" /]  --]


                [#-- ****** stage ****** --]
                [#-- <h2>Single-Area Stage</h2> -> Heading needs to be moved into the Area script for the correct div around --]
                [@cms.area name="stage" /]

            </div><!-- end header -->

            <h1>${content.title!content.@name}</h1>
            <p>${content.@path} (${content.@id})</p>
            <p>From JCR NODE: ${cmsfn.asJCRNode(content).path} </p>


            <div id="wrapper-2">

                [#-- ****** main ****** --]
                <h2>List-Area Main</h2>
                [@cms.area name="main" /]


                [#-- ****** extras ****** --]
                <h2>List-Area Extras</h2>
                [@cms.area name="extras" /]

            </div><!-- end wrapper-2 -->

            [#-- ****** footer  ****** --]
            [#-- <h2>List-Area Footer</h2> -> Heading needs to be moved into the Area script for the correct div around --]
            [@cms.area name="footer" /]


        </div><!-- end wrapper -->
    </body>
</html>
