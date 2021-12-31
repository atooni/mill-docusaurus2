# Simple MDoc runner for Mill

This plugin provides a mill module that allows to build the project web site using 
[Docusaurus 2](https://docusaurus.io) as a static content generator. Even though it is 
possible to run Docusaurus without [Mill MDoc](https://github.com/atooni/mill-mdoc), 
it is useful to use both plugins in combination to ensure the documentation contains 
valid code. 

## Parameters 

### `docusaurusSourcess`

A `Sources` parameter that points to the directory that contains all required docusaurus 
config files and static resources required to build the site. 

These resources will be copied to the appropriate folder in mill's `out` directory where 
they will be merged with the `compiledMdocs` The result of this merge is used to build 
the site. 

### `compiledMdocs`

A `Sources` parameter pointing to the mdoc sources making up the contant of the site. 

## Targets

### `yarnInstall`

Prepare a `node_modules` directory by running `yarn install` based on the content of 
the sources in `docusaurusSources`.

### `docusaurusBuild`

Execute `docusaurusBuild` using the prpeared `node_modules` and the content of `compiledMdocs`. 

## Example usage  

```scala
  //... Other imports if requried 

  // Add simple mdoc support for mill
  import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.mdoc::0.0.1-1-fdff74`
  import de.wayofquality.mill.mdoc.MDocModule

  // It's convenient to keep the base project directory around
  val projectDir = build.millSourcePath

  object site extends DocusaurusModule with MDocModule {
    // Set the Scala version (required to invoke the proper compiler)
    override def scalaVersion = T(Deps.scalaVersion)
    // The md inputs live in the "docs" folder of the project 
    override def mdocSources = T.sources{ projectDir / "docs" }
    override def docusaurusSources = T.sources(
      projectDir / "website",
    )

    // If we are running docusaurus in watch mode we want to replace compiled 
    // mdoc files on the fly - this will NOT build md files for the site
    // Hence we must use `mdoc` once we finished editing.
    override def watchedMDocsDestination: T[Option[Path]] = T(Some(docusaurusBuild().path / "docs"))

    // This is where docusaurus will find the compiled mdocs to BUILD the site
    override def compiledMdocs: Sources = T.sources(mdoc().path)
}

```

With the example above, mill will create a complete docusaurus folder in `out/site/docusaurusBuild/dest`.
Note, that the parameter `watchedMDocsDestination` is set to the `docs` folder within that directory. 

As a result the docs folder will be contiously updated while the docs in `$projectDir/docs`
are edited and an instance of mill is running the mdoc plugin in watch mode. 

If a docusaurus instance is started from `out/site/docusaurusBuild/dest` in watch mode, documentation changes 
will be reflected in the browser almost immediately after saving them.

## Tip

To work on the documentation it is best, to run run an instance of mill watching the `md` files 
in the `docs` directory, compiling them whenever changes have been saved. While doing this, 
point the `watchedMDocsDestination` parameter to `(docusaurusBuild()/path) / "docs"` and start
docusaurus in watch mode. 

Then the changes made in the `md` files will be reflected immediately in the browser.