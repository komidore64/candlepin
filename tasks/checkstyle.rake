require 'buildr/checkstyle'
require 'rexml/document'

module AntTaskCheckstyle
  class << self
    def dependencies
      Buildr.transitive('com.puppycrawl.tools:checkstyle:jar:5.7')
    end

    def checkstyle(conf_file, format, output_file, source_paths, options={})
      dependencies = (options[:dependencies] || []) + AntTaskCheckstyle.dependencies
      cp = Buildr.artifacts(dependencies).each { |a| a.invoke() if a.respond_to?(:invoke) }.map(&:to_s).join(File::PATH_SEPARATOR)
      options[:properties] ||= {}
      options[:profiles] ||= []
      begin
        info("Running Checkstyle on #{options[:project]}")

        # See http://checkstyle.sourceforge.net/anttask.html
        Buildr.ant('checkstyle') do |ant|
          ant.taskdef(:classpath => cp, :resource => "checkstyletask.properties")
          options[:profiles].each do |profile|
            next unless profile.enabled == "true"

            profile_properties = options[:properties].merge(profile.properties)
            profile.properties.values.map! do |v|
              # basedir is set in the properties method of Config's superclass: Buildr::Checkstyle::Config
              v.gsub!("${basedir}", profile_properties[:basedir])
            end

            patterns = profile.patterns || Pattern.new(".*\.java", true)
            ant.checkstyle(:classpath => cp, :config => conf_file,
                :failOnViolation => false, :failureProperty => 'checkstyleFailed') do
              format_opts = { :type => format }
              format_opts[:toFile] = output_file unless output_file.nil?
              ant.formatter(format_opts)
              profile.properties.each do |k, v|
                ant.property(:key => k, :value => v)
              end
              source_paths.each do |source_path|
                ant.fileset(:dir => source_path) do
                  patterns.each do |pattern|
                    ant.filename(:regex => pattern.pattern, :negate => !pattern.is_include)
                    if pattern.is_include
                      info("Checking #{File.join(source_path, pattern.pattern)}")
                    end
                  end
                end
              end
            end
          end
          fail("Checkstyle failed") if ant.project.getProperty('checkstyleFailed')
        end
      rescue => e
        warn("Checkstyle found errors")
        raise e if options[:fail_on_error]
      end
    end
  end

  class Profile < Struct.new(:name, :enabled, :properties, :patterns)
  end

  class Pattern < Struct.new(:pattern, :is_include)
  end

  class Config < Buildr::Checkstyle::Config
    attr_writer :eclipse_xml
    def eclipse_xml
      @eclipse_xml || project.path_to(".checkstyle")
    end

    attr_writer :format
    def format
      @format || "plain"
    end

    attr_writer :source_paths
    def source_paths
      # The filesets in the Eclipse checkstyle config will take care of the
      # particulars, so we just want to start at the top
      @source_paths || [project.base_dir]
    end

    attr_writer :fail_on_error
    def fail_on_error?
      (@fail_on_error.nil?) ? true : @fail_on_error
    end

    attr_writer :plain_output_file
    def plain_output_file
      # nil will write to stdout
      @plain_output_file || nil
    end

    # This overrides the super class which adds project.compile.dependencies
    # and project.test.compile.dependencies by default.  We don't need all that
    # stuff on the classpath because we aren't even compiling the project.
    def extra_dependencies
      @extra_dependencies || []
    end

    attr_writer :properties
    def properties
      unless @properties
        @properties = super
      end
      @properties
    end

    def eclipse_properties
      unless File.exist?(eclipse_xml)
        warn("Could not find #{eclipse_xml} for #{project}.  This is probably an error.") if enabled?
        return {}
      end

      return @eclipse_properties unless @eclipse_properties.nil?
      @eclipse_properties = []

      doc = REXML::Document.new(File.new(eclipse_xml))

      property_sets = {}
      configs = REXML::XPath.match(doc, "/fileset-config/local-check-config")
      configs.each do |c|
        props = {}
        c.get_elements('property').inject(props) do |h, el|
          # The .value() call is important.  It's what expands the XML entity we
          # use to point to the config directory.
          h[el.attribute('name').to_s] = el.attribute('value').value
          h
        end
        property_sets[c.attribute('name').to_s] = props
      end

      filesets = REXML::XPath.match(doc, "/fileset-config/fileset")
      filesets.each do |fs|
        profile = Profile.new
        profile.name = fs.attribute('name').to_s
        profile.enabled = fs.attribute('enabled').to_s
        profile.properties = property_sets[fs.attribute('check-config-name').to_s]
        patterns = []
        profile.patterns = fs.get_elements('file-match-pattern').inject(patterns) do |p, fmp|
          if fmp.attribute('include-pattern').to_s == "true"
            pattern_type = true
          else
            pattern_type = false
          end
          p << Pattern.new(fmp.attribute('match-pattern').to_s, pattern_type)
          p
        end
        @eclipse_properties << profile
      end

      return @eclipse_properties
    end
  end

  module ProjectExtension
    include Extension

    def checkstyle
      @checkstyle ||= AntTaskCheckstyle::Config.new(project)
    end

    first_time do
      desc "Run Checkstyle"
      Project.local_task('checkstyle')

      desc "Create Checkstyle XML report"
      Project.local_task('checkstyle:xml')

      desc "Create Checkstyle HTML report"
      Project.local_task('checkstyle:html')
    end

    after_define do |project|
      cs = project.checkstyle
      ide = project.eclipse
      if cs.enabled?
        unless ide.natures.empty?
          ide.natures('net.sf.eclipsecs.core.CheckstyleNature')
          ide.builders('net.sf.eclipsecs.core.CheckstyleBuilder')
        end

        task('checkstyle:xml').clear
        task('checkstyle:html').clear

        project.recursive_task('checkstyle') do |task|
          AntTaskCheckstyle.checkstyle(cs.configuration_file,
                                       'plain',
                                       cs.plain_output_file,
                                       cs.source_paths.flatten.compact,
                                       :profiles => cs.eclipse_properties,
                                       :properties => cs.properties,
                                       :fail_on_error => cs.fail_on_error?,
                                       :dependencies => cs.extra_dependencies,
                                       :project => project)
        end

        project.recursive_task('checkstyle:xml') do |task|
          reports_dir = project.path_to(:reports, :checkstyle)
          rm_rf(reports_dir)
          mkdir_p(reports_dir)
          AntTaskCheckstyle.checkstyle(cs.configuration_file,
                                       'xml',
                                       cs.xml_output_file,
                                       cs.source_paths.flatten.compact,
                                       :profiles => cs.eclipse_properties,
                                       :properties => cs.properties,
                                       :fail_on_error => false,
                                       :dependencies => cs.extra_dependencies,
                                       :project => project)
        end
        if cs.html_enabled?
          project.recursive_task('checkstyle:html' => 'checkstyle:xml') do |task|
            info("Converting XML to HTML")
            mkdir_p(File.dirname(cs.html_output_file))
            Buildr.ant('checkstyle') do |ant|
              ant.xslt(:in => cs.xml_output_file,
                       :out => cs.html_output_file,
                       :style => cs.style_file)
            end
          end
        end
      end
    end
  end
end

class Buildr::Project
  include AntTaskCheckstyle::ProjectExtension
end
